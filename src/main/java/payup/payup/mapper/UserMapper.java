package payup.payup.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import payup.payup.dto.UserDto;
import payup.payup.model.User;

@Component
public class UserMapper {
    private static final Logger logger = LoggerFactory.getLogger(UserMapper.class);

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        return dto;
    }

    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        if (dto.getName() != null) {
            user.setName(dto.getName());
        } else {
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
        }
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPassword(dto.getPassword());
        if (dto.getRole() != null) {
            try {
                user.setRole(User.UserRole.valueOf(dto.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.error("Invalid role value: {}", dto.getRole());
                throw new IllegalArgumentException("Invalid role: " + dto.getRole());
            }
        }
        logger.debug("Mapped User: {}", user);
        return user;
    }
}