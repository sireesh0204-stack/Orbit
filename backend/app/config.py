import os
from typing import List
from pydantic import BaseModel, Field

class Settings(BaseModel):
    app_name: str = "JobPilot Backend"
    version: str = "1.0.0"
    environment: str = Field(default_factory=lambda: os.getenv("ENVIRONMENT", "development"))
    port: int = Field(default_factory=lambda: int(os.getenv("PORT", "8080")))
    
    # Dual-user whitelist restriction
    # Can be configured via env var ALLOWED_USER_EMAILS="email1@example.com,email2@example.com"
    allowed_user_emails: List[str] = Field(
        default_factory=lambda: [
            email.strip().lower()
            for email in os.getenv(
                "ALLOWED_USER_EMAILS", 
                "sireesh0204@gmail.com,bhargaviyerroju@outlook.com"
            ).split(",")
            if email.strip()
        ]
    )
    
    allowed_user_uids: List[str] = Field(
        default_factory=lambda: [
            uid.strip()
            for uid in os.getenv(
                "ALLOWED_USER_UIDS",
                "firebase_uid_user_1,firebase_uid_user_2"
            ).split(",")
            if uid.strip()
        ]
    )

    # Auth flags
    mock_auth: bool = Field(
        default_factory=lambda: os.getenv("MOCK_AUTH", "false").lower() in ("1", "true", "yes")
    )
    firebase_credentials_json: str = Field(
        default_factory=lambda: os.getenv("FIREBASE_CREDENTIALS_JSON", "")
    )

settings = Settings()

