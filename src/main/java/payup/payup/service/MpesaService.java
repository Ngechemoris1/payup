package payup.payup.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payup.payup.config.MpesaConfig;
import payup.payup.dto.PaymentResponseDto; // Added for response
import payup.payup.model.Payment;
import payup.payup.model.Tenant;
import payup.repository.PaymentRepository;
import payup.repository.TenantRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.List;

@Service
public class MpesaService {

    private static final Logger logger = LoggerFactory.getLogger(MpesaService.class);
    private static final String BASE_URL = "https://%s.safaricom.co.ke";
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_DELAY_MS = 1000;

    @Autowired
    private MpesaConfig mpesaConfig;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String accessToken;
    private long tokenExpiryTime;

    @Transactional
    public synchronized String getAccessToken() throws IOException {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            logger.info("Fetching new M-Pesa access token");
            String credentials = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            String url = String.format(BASE_URL, mpesaConfig.getEnvironment()) + "/oauth/v1/generate?grant_type=client_credentials";

            int delay = INITIAL_DELAY_MS;
            for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("Authorization", "Basic " + encodedCredentials)
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            JsonNode jsonNode = objectMapper.readTree(response.body().string());
                            accessToken = jsonNode.get("access_token").asText();
                            long expiresIn = jsonNode.get("expires_in").asLong();
                            tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000) - 60000;
                            logger.info("Access token fetched, expires in {} seconds", expiresIn);
                            return accessToken;
                        }
                    }
                } catch (IOException e) {
                    logger.warn("Token fetch attempt {}/{} failed: {}", attempt + 1, MAX_RETRIES, e.getMessage());
                    if (attempt == MAX_RETRIES - 1) throw e;
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        logger.error("Thread sleep interrupted: {}", interruptedException.getMessage());
                    }
                    delay *= 2;
                }
            }
        }
        return accessToken;
    }

    @Transactional
    public PaymentResponseDto initiatePayment(Long tenantId, Double amount, String phoneNumber, Long billId) throws IOException {
        validatePaymentRequest(tenantId, amount, phoneNumber);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

        String accessToken = getAccessToken();
        String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
        String password = generatePassword(timestamp);
        String idempotencyKey = UUID.randomUUID().toString();

        Map<String, String> requestBody = buildStkPushPayload(amount, phoneNumber, timestamp, password, idempotencyKey, tenantId, billId);
        String jsonPayload = objectMapper.writeValueAsString(requestBody);
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

        String url = String.format(BASE_URL, mpesaConfig.getEnvironment()) + "/mpesa/stkpush/v1/processrequest";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("STK Push failed: HTTP {} - {}", response.code(), response.message());
                throw new IOException("STK Push failed: " + response.code());
            }

            JsonNode responseJson = objectMapper.readTree(response.body().string());
            String checkoutRequestId = responseJson.get("CheckoutRequestID").asText();

            savePayment(tenant, BigDecimal.valueOf(amount), checkoutRequestId, idempotencyKey, billId);
            logger.info("STK Push initiated: checkoutRequestId={}", checkoutRequestId);
            return new PaymentResponseDto(checkoutRequestId, "Payment initiated successfully");
        }
    }

    @Async
    @Transactional
    public void handleCallback(Map<String, Object> callbackData) {
        logger.info("Processing M-Pesa callback: {}", callbackData);
        if (!validateCallback(callbackData)) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.convertValue(callbackData.get("Body"), Map.class);
        Map<String, Object> stkCallback = objectMapper.convertValue(body.get("stkCallback"), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        String checkoutRequestId = (String) stkCallback.get("CheckoutRequestID");
        int resultCode = (Integer) stkCallback.get("ResultCode");

        Payment payment = paymentRepository.findByTransactionId(checkoutRequestId)
                .orElseThrow(() -> {
                    logger.error("Payment not found: checkoutRequestId={}", checkoutRequestId);
                    return new RuntimeException("Payment not found");
                });

        processCallbackResult(payment, resultCode, stkCallback);
        paymentRepository.save(payment);
        logger.info("Callback processed: checkoutRequestId={}, status={}", checkoutRequestId, payment.getStatus());
    }

    private void validatePaymentRequest(Long tenantId, Double amount, String phoneNumber) {
        if (tenantId == null || amount <= 0 || !phoneNumber.matches("^254[7-9][0-9]{8}$")) {
            logger.error("Invalid payment request: tenantId={}, amount={}, phoneNumber={}", tenantId, amount, phoneNumber);
            throw new IllegalArgumentException("Invalid payment details");
        }
        if (amount < 1 || amount > 150000) {
            logger.error("Amount out of bounds: {}", amount);
            throw new IllegalArgumentException("Amount must be between KES 1 and KES 150,000");
        }
    }

    private Map<String, String> buildStkPushPayload(Double amount, String phoneNumber, String timestamp, String password, String idempotencyKey, Long tenantId, Long billId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("BusinessShortCode", mpesaConfig.getShortcode());
        payload.put("Password", password);
        payload.put("Timestamp", timestamp);
        payload.put("TransactionType", "CustomerPayBillOnline");
        payload.put("Amount", String.valueOf(amount.intValue())); // M-Pesa expects an integer amount
        payload.put("PartyA", phoneNumber);
        payload.put("PartyB", mpesaConfig.getShortcode());
        payload.put("PhoneNumber", phoneNumber);
        payload.put("CallBackURL", mpesaConfig.getCallbackUrl());
        payload.put("AccountReference", billId != null ? "Bill-" + billId : "payup-" + tenantId);
        payload.put("TransactionDesc", billId != null ? "Bill Payment" : "Rent Payment");
        payload.put("IdempotencyKey", idempotencyKey);
        return payload;
    }

    private void savePayment(Tenant tenant, BigDecimal amount, String checkoutRequestId, String idempotencyKey, Long billId) {
        Payment payment = new Payment();
        payment.setTenant(tenant);
        payment.setAmount(amount);
        payment.setPaymentMethod(Payment.PaymentMethod.MPESA);
        payment.setTransactionId(checkoutRequestId);
        payment.setStatus(Payment.Status.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setIdempotencyKey(idempotencyKey);
        if (billId != null) {
            // Assuming a Bill entity and repository exist; you'd need to fetch and set it
            // payment.setBill(billRepository.findById(billId).orElse(null));
        }
        paymentRepository.save(payment);
    }

    private boolean validateCallback(Map<String, Object> callbackData) {
        if (callbackData == null || !callbackData.containsKey("Body")) {
            logger.error("Invalid callback data: missing 'Body'");
            return false;
        }
        return true;
    }

    private void processCallbackResult(Payment payment, int resultCode, Map<String, Object> stkCallback) {
        switch (resultCode) {
            case 0:
                Map<String, Object> metadata = (Map<String, Object>) stkCallback.get("CallbackMetadata");
                List<Map<String, Object>> items = (List<Map<String, Object>>) metadata.get("Item");
                String mpesaReceipt = items.stream()
                        .filter(item -> "MpesaReceiptNumber".equals(item.get("Name")))
                        .map(item -> (String) item.get("Value"))
                        .findFirst().orElse("N/A");
                payment.setStatus(Payment.Status.PAID);
                payment.setMpesaReceiptNumber(mpesaReceipt);
                payment.setPaidAt(LocalDateTime.now());
                break;
            default:
                payment.setStatus(Payment.Status.FAILED);
                logger.warn("Payment failed: resultCode={}", resultCode);
                break;
        }
    }

    private String generatePassword(String timestamp) {
        String rawPassword = mpesaConfig.getShortcode() + mpesaConfig.getPasskey() + timestamp;
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }
}