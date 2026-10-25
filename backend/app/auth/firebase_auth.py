import os
import json
import base64
from typing import Optional, Dict, Any
from fastapi import Header, HTTPException, status, Depends
from app.config import settings

# Initialize Firebase Admin if credentials are present
try:
    import firebase_admin
    from firebase_admin import credentials, auth
    if not firebase_admin._apps:
        creds_path = os.path.join(
            os.path.dirname(os.path.dirname(os.path.dirname(__file__))),
            "firebase_credentials.json"
        )
        if os.path.exists(creds_path):
            cred = credentials.Certificate(creds_path)
            firebase_admin.initialize_app(cred)
except Exception:
    pass

class AuthenticatedUser:
    def __init__(self, uid: str, email: str, name: Optional[str] = None):
        self.uid = uid
        self.email = email
        self.name = name or email.split("@")[0]

    def to_dict(self) -> Dict[str, Any]:
        return {
            "uid": self.uid,
            "email": self.email,
            "name": self.name
        }

def decode_mock_token(token: str) -> Dict[str, Any]:
    """
    Decodes simulated/test tokens in mock/dev mode.
    Format can be standard JWT (header.payload.signature) or raw base64 json.
    """
    try:
        parts = token.split(".")
        if len(parts) >= 2:
            # Base64 decode payload
            payload_b64 = parts[1]
            # Add padding if needed
            rem = len(payload_b64) % 4
            if rem > 0:
                payload_b64 += "=" * (4 - rem)
            payload_json = base64.urlsafe_b64decode(payload_b64.encode("utf-8")).decode("utf-8")
            return json.loads(payload_json)
        else:
            # Fallback for plain json or test strings
            return json.loads(token)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Invalid authentication token format: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )

def verify_firebase_token(token: str) -> Dict[str, Any]:
    if settings.mock_auth:
        return decode_mock_token(token)

    try:
        import firebase_admin
        from firebase_admin import auth
        # Verify with live Firebase Admin SDK
        decoded_token = auth.verify_id_token(token)
        return decoded_token
    except Exception as e:
        # If firebase not initialized or mock fallback requested
        if settings.environment in ("development", "test"):
            return decode_mock_token(token)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Firebase token verification failed: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )

async def get_current_user(
    authorization: Optional[str] = Header(None)
) -> AuthenticatedUser:
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization header missing",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    parts = authorization.split()
    if len(parts) != 2 or parts[0].lower() != "bearer":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authorization header format. Expected 'Bearer <token>'",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    token = parts[1]
    decoded = verify_firebase_token(token)
    
    uid = decoded.get("uid") or decoded.get("sub") or decoded.get("user_id")
    email = (decoded.get("email") or "").strip().lower()
    name = decoded.get("name")
    
    if not uid:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token missing user identifier (uid)",
            headers={"WWW-Authenticate": "Bearer"},
        )
        
    return AuthenticatedUser(uid=uid, email=email, name=name)

async def verify_whitelisted_user(
    current_user: AuthenticatedUser = Depends(get_current_user)
) -> AuthenticatedUser:
    """
    Strict Whitelist Invariant Gate:
    Only the 2 authorized user emails/UIDs can proceed.
    """
    is_whitelisted = (
        (current_user.email and current_user.email in settings.allowed_user_emails) or
        (current_user.uid in settings.allowed_user_uids)
    )
    
    if not is_whitelisted:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access forbidden: user is not on the private JobPilot whitelist.",
        )
    
    return current_user

