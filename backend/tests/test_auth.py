def test_profile_no_auth_header_returns_401(client):
    response = client.get("/api/v1/profile")
    assert response.status_code == 401
    assert "Authorization header missing" in response.json()["detail"]

def test_profile_malformed_auth_header_returns_401(client):
    response = client.get(
        "/api/v1/profile", 
        headers={"Authorization": "NotBearer some_token"}
    )
    assert response.status_code == 401
    assert "Invalid authorization header format" in response.json()["detail"]

def test_profile_unauthorized_user_returns_403(client, auth_headers_unauthorized):
    response = client.get("/api/v1/profile", headers=auth_headers_unauthorized)
    assert response.status_code == 403
    assert "not on the private JobPilot whitelist" in response.json()["detail"]

def test_profile_whitelisted_user1_returns_200(client, auth_headers_user1):
    response = client.get("/api/v1/profile", headers=auth_headers_user1)
    assert response.status_code == 200
    data = response.json()
    assert data["user_id"] == "firebase_uid_user_1"

def test_profile_whitelisted_user2_returns_200(client, auth_headers_user2):
    response = client.get("/api/v1/profile", headers=auth_headers_user2)
    assert response.status_code == 200
    data = response.json()
    assert data["user_id"] == "firebase_uid_user_2"

def test_jobs_unauthorized_user_returns_403(client, auth_headers_unauthorized):
    response = client.get("/api/v1/jobs", headers=auth_headers_unauthorized)
    assert response.status_code == 403

def test_jobs_whitelisted_user_returns_200(client, auth_headers_user1):
    response = client.get("/api/v1/jobs", headers=auth_headers_user1)
    assert response.status_code == 200
    assert isinstance(response.json(), list)

