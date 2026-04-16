const SESSION_KEY = "local-events-session";
const PROFILE_INDEX_KEY = "eventradar-user-profiles";

export function loadSession() {
    try {
        const raw = localStorage.getItem(SESSION_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

export function saveSession(session) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession() {
    localStorage.removeItem(SESSION_KEY);
}

function loadProfileIndex() {
    try {
        const raw = localStorage.getItem(PROFILE_INDEX_KEY);
        return raw ? JSON.parse(raw) : {};
    } catch {
        return {};
    }
}

function saveProfileIndex(index) {
    localStorage.setItem(PROFILE_INDEX_KEY, JSON.stringify(index));
}

export function getStoredProfile(authUserId) {
    if (!authUserId) {
        return null;
    }

    const index = loadProfileIndex();
    return index[String(authUserId)] || null;
}

export function saveStoredProfile(authUserId, profile) {
    if (!authUserId) {
        return;
    }

    const index = loadProfileIndex();
    index[String(authUserId)] = profile;
    saveProfileIndex(index);
}
