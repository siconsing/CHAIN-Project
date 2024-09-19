        package org.zerock.chain.exception;


        import org.springframework.http.HttpStatus;
        import org.springframework.web.bind.annotation.ResponseStatus;


        @ResponseStatus(HttpStatus.NOT_FOUND)
        public class AttendanceRecordNotFoundException extends RuntimeException {
            public AttendanceRecordNotFoundException(String message) {
                super(message);
            }
        }
