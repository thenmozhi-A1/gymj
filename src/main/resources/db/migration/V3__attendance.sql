DROP TABLE IF EXISTS attendance;
CREATE TABLE attendance (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id          BIGINT NULL,
  staff_id         BIGINT NULL,
  attendance_date  DATE NOT NULL,
  check_in_time    TIME NOT NULL,
  check_out_time   TIME NULL,
  method           ENUM('MANUAL','QR','FINGERPRINT') NOT NULL DEFAULT 'MANUAL',
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_att_user  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
  CONSTRAINT fk_att_staff FOREIGN KEY (staff_id) REFERENCES staff_details(id) ON DELETE CASCADE,
  CONSTRAINT chk_att_one_owner CHECK (
    (user_id IS NOT NULL AND staff_id IS NULL) OR
    (user_id IS NULL AND staff_id IS NOT NULL)
  )
);

CREATE INDEX idx_att_user_date  ON attendance (user_id,  attendance_date);
CREATE INDEX idx_att_staff_date ON attendance (staff_id, attendance_date);
CREATE INDEX idx_att_date       ON attendance (attendance_date);
