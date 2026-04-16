async function parseJson(response) {
    const text = await response.text();
    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
}

async function request(url, options = {}) {
    const response = await fetch(url, options);
    const data = await parseJson(response);

    if (!response.ok) {
        throw new Error(data?.error || data?.message || `Request failed with ${response.status}`);
    }

    return data;
}

export function buildAuthHeaders(session) {
    return {
        "Content-Type": "application/json",
        Authorization: `Bearer ${session.token}`
    };
}

export function registerAuth(payload) {
    return request("/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
}

export function loginAuth(payload) {
    return request("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
}

export function createBusiness(payload, session) {
    return request("/api/businesses", {
        method: "POST",
        headers: buildAuthHeaders(session),
        body: JSON.stringify(payload)
    });
}

export function createEvent(payload, session) {
    return request("/api/events", {
        method: "POST",
        headers: buildAuthHeaders(session),
        body: JSON.stringify(payload)
    });
}

export function updateEvent(eventId, payload, session) {
    return request(`/api/events/${eventId}`, {
        method: "PUT",
        headers: buildAuthHeaders(session),
        body: JSON.stringify(payload)
    });
}

export function deleteEvent(eventId, session) {
    return request(`/api/events/${eventId}`, {
        method: "DELETE",
        headers: buildAuthHeaders(session)
    });
}

export function createUserProfile(payload, session) {
    return request("/api/users", {
        method: "POST",
        headers: buildAuthHeaders(session),
        body: JSON.stringify(payload)
    });
}

export function addFavorite(profileUserId, eventId, session) {
    return request(`/api/users/${profileUserId}/favorites/events/${eventId}`, {
        method: "POST",
        headers: buildAuthHeaders(session)
    });
}

export function removeFavorite(profileUserId, eventId, session) {
    return request(`/api/users/${profileUserId}/favorites/events/${eventId}`, {
        method: "DELETE",
        headers: buildAuthHeaders(session)
    });
}

export function getFavoriteIds(profileUserId, session) {
    return request(`/api/users/${profileUserId}/favorites/events`, {
        headers: buildAuthHeaders(session)
    });
}

export function getUserProfile(profileUserId, session) {
    return request(`/api/users/${profileUserId}`, {
        headers: buildAuthHeaders(session)
    });
}

export function updateUserProfile(profileUserId, payload, session) {
    return request(`/api/users/${profileUserId}`, {
        method: "PUT",
        headers: buildAuthHeaders(session),
        body: JSON.stringify(payload)
    });
}

export function getEvent(eventId) {
    return request(`/api/events/${eventId}`);
}

export function getVenueAvailability(venue, date) {
    const params = new URLSearchParams();
    if (date) params.set("date", date);
    const suffix = params.toString() ? `?${params.toString()}` : "";
    return request(`/api/events/venues/${encodeURIComponent(venue)}/availability${suffix}`);
}

export function getExportLinks(eventId) {
    return request(`/api/exports/events/${eventId}/link`);
}

export function getEvents(filters = {}) {
    const params = new URLSearchParams();

    if (filters.city) params.set("city", filters.city);
    if (filters.date) params.set("date", filters.date);
    if (filters.businessId) params.set("businessId", filters.businessId);
    if (filters.page) params.set("page", filters.page);
    if (filters.limit) params.set("limit", filters.limit);

    const suffix = params.toString() ? `?${params.toString()}` : "";
    return request(`/api/events${suffix}`);
}
