package models.update;

public record SuccessfulUpdateResponseModel(Integer id, String username, String firstName,
                                            String lastName, String email, String remoteAddr) {
}
