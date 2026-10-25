import pytest
import json
import base64
from fastapi.testclient import TestClient
from app.main import app
from app.config import settings

def make_test_token(uid: str, email: str, name: str = "Test User") -> str:
    """Creates a base64-encoded mock JWT header.payload.sig string."""
    header = {"alg": "none", "typ": "JWT"}
    payload = {
        "uid": uid,
        "sub": uid,
        "email": email,
        "name": name,
        "iss": "https://securetoken.google.com/test-project",
        "aud": "test-project"
    }
    
    def b64url(data: dict) -> str:
        s = json.dumps(data)
        return base64.urlsafe_b64encode(s.encode("utf-8")).decode("utf-8").rstrip("=")
        
    return f"{b64url(header)}.{b64url(payload)}.mock_sig"

@pytest.fixture(autouse=True)
def configure_mock_auth(monkeypatch):
    monkeypatch.setattr(settings, "mock_auth", True)
    monkeypatch.setattr(settings, "environment", "test")
    monkeypatch.setattr(settings, "allowed_user_emails", [
        "pilot.user1@example.com",
        "pilot.user2@example.com"
    ])
    monkeypatch.setattr(settings, "allowed_user_uids", [
        "firebase_uid_user_1",
        "firebase_uid_user_2"
    ])

@pytest.fixture
def client():
    return TestClient(app)

@pytest.fixture
def auth_headers_user1():
    token = make_test_token("firebase_uid_user_1", "pilot.user1@example.com", "Pilot One")
    return {"Authorization": f"Bearer {token}"}

@pytest.fixture
def auth_headers_user2():
    token = make_test_token("firebase_uid_user_2", "pilot.user2@example.com", "Pilot Two")
    return {"Authorization": f"Bearer {token}"}

@pytest.fixture
def auth_headers_unauthorized():
    token = make_test_token("hacker_uid", "intruder@stranger.com", "Intruder")
    return {"Authorization": f"Bearer {token}"}

