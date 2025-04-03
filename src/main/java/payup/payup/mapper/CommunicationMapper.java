package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.CommunicationDto;
import payup.payup.model.Communication;

@Component
public class CommunicationMapper {

    public CommunicationDto toDto(Communication communication) {
        if (communication == null) {
            return null;
        }

        CommunicationDto dto = new CommunicationDto();
        dto.setId(communication.getId());
        dto.setMessage(communication.getMessage());
        dto.setType(communication.getType().name());
        dto.setSentAt(communication.getSentAt());

        if (communication.getSender() != null) {
            dto.setSenderId(communication.getSender().getId());
        }
        if (communication.getReceiver() != null) {
            dto.setReceiverId(communication.getReceiver().getId());
        }

        return dto;
    }

    public Communication toEntity(CommunicationDto dto) {
        if (dto == null) {
            return null;
        }

        Communication communication = new Communication();
        communication.setId(dto.getId());
        communication.setMessage(dto.getMessage());
        if (dto.getType() != null) {
            communication.setType(Communication.MessageType.valueOf(dto.getType()));
        }
        communication.setSentAt(dto.getSentAt());
        // Sender and receiver should be set separately via service
        return communication;
    }
}
