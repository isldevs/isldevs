ALTER TABLE users ADD COLUMN isAccountNonExpired boolean NOT NULL DEFAULT true;
ALTER TABLE users ADD COLUMN isAccountNonLocked boolean NOT NULL DEFAULT true;
ALTER TABLE users ADD COLUMN isCredentialsNonExpired boolean NOT NULL DEFAULT true;