const eventList = document.getElementById("eventList");
const statusBadge = document.getElementById("statusBadge");
const refreshButton = document.getElementById("refreshButton");
const emptyState = document.getElementById("emptyState");
const eventDetail = document.getElementById("eventDetail");
const exportStatus = document.getElementById("exportStatus");

const detailBusiness = document.getElementById("detailBusiness");
const detailTitle = document.getElementById("detailTitle");
const detailDate = document.getElementById("detailDate");
const detailTime = document.getElementById("detailTime");
const detailVenue = document.getElementById("detailVenue");
const detailId = document.getElementById("detailId");
const detailDescription = document.getElementById("detailDescription");

const googleLink = document.getElementById("googleLink");
const outlookLink = document.getElementById("outlookLink");
const icsLink = document.getElementById("icsLink");

const registerForm = document.getElementById("registerForm");
const loginForm = document.getElementById("loginForm");
const logoutButton = document.getElementById("logoutButton");
const authStatus = document.getElementById("authStatus");
const sessionBadge = document.getElementById("sessionBadge");
const sessionUserId = document.getElementById("sessionUserId");
const sessionEmail = document.getElementById("sessionEmail");
const sessionRole = document.getElementById("sessionRole");

const businessForm = document.getElementById("businessForm");
const eventForm = document.getElementById("eventForm");
const protectedStatus = document.getElementById("protectedStatus");
const businessContactEmail = document.getElementById("businessContactEmail");
const eventBusinessId = document.getElementById("eventBusinessId");
const toastContainer = document.getElementById("toastContainer");

let selectedEventId = null;
let session = loadSession();

console.log("APP_JS_VERSION_777777");

refreshButton.addEventListener("click", () => {
    loadEvents();
});

document.addEventListener("DOMContentLoaded", () => {
    wireAuthForms();
    wireProtectedForms();
    updateSessionUi();
    prefillDemoValues();
    loadEvents();
});

function wireAuthForms() {
    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        authStatus.textContent = "Creating auth account...";

        const payload = {
            email: document.getElementById("registerEmail").value.trim(),
            password: document.getElementById("registerPassword").value,
            role: document.getElementById("registerRole").value
        };

        try {
            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const data = await parseJson(response);
            if (!response.ok) {
                throw new Error(data?.error || data?.message || `Register failed with ${response.status}`);
            }

            authStatus.textContent = `Account created for ${data.email}. You can log in now.`;
            showToast("Account created", `Registered ${data.email} as ${data.role}.`, "success");
            document.getElementById("loginEmail").value = payload.email;
            document.getElementById("loginPassword").value = payload.password;
            if (payload.role === "BUSINESS") {
                businessContactEmail.value = payload.email;
            }
            registerForm.reset();
        } catch (error) {
            authStatus.textContent = error.message;
            showToast("Register failed", error.message, "error");
        }
    });

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        authStatus.textContent = "Signing in...";

        const payload = {
            email: document.getElementById("loginEmail").value.trim(),
            password: document.getElementById("loginPassword").value
        };

        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const data = await parseJson(response);
            if (!response.ok) {
                throw new Error(data?.error || data?.message || `Login failed with ${response.status}`);
            }

            session = {
                token: data.token,
                userId: data.userId,
                role: data.role,
                email: payload.email
            };
            saveSession(session);
            updateSessionUi();
            authStatus.textContent = `Logged in as ${payload.email}.`;
            showToast("Login successful", `Session active for ${payload.email}.`, "success");
            if (session.role === "BUSINESS") {
                businessContactEmail.value = session.email;
            }
        } catch (error) {
            authStatus.textContent = error.message;
            showToast("Login failed", error.message, "error");
        }
    });

    logoutButton.addEventListener("click", () => {
        clearSession();
        session = null;
        updateSessionUi();
        authStatus.textContent = "Session cleared in the browser.";
        showToast("Logged out", "The local browser session was cleared.", "success");
    });
}

function wireProtectedForms() {
    businessForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!ensureBusinessSession()) {
            return;
        }

        protectedStatus.textContent = "Creating business...";

        const payload = {
            name: document.getElementById("businessName").value.trim(),
            address: document.getElementById("businessAddress").value.trim(),
            city: document.getElementById("businessCity").value.trim(),
            contactEmail: businessContactEmail.value.trim(),
            phone: document.getElementById("businessPhone").value.trim(),
            description: document.getElementById("businessDescription").value.trim()
        };

        try {
            const response = await fetch("/api/businesses", {
                method: "POST",
                headers: buildAuthHeaders(),
                body: JSON.stringify(payload)
            });

            const data = await parseJson(response);
            if (!response.ok) {
                throw new Error(data?.error || data?.message || `Business creation failed with ${response.status}`);
            }

            eventBusinessId.value = data.businessId;
            protectedStatus.textContent = `Business created with id ${data.businessId}.`;
            showToast("Business created", `${data.name} is ready with business ID ${data.businessId}.`, "success");
        } catch (error) {
            protectedStatus.textContent = error.message;
            showToast("Business creation failed", error.message, "error");
        }
    });

    eventForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!ensureBusinessSession()) {
            return;
        }

        protectedStatus.textContent = "Creating event...";

        const payload = {
            title: document.getElementById("eventTitleInput").value.trim(),
            description: document.getElementById("eventDescriptionInput").value.trim(),
            startTime: toUtcIsoString(document.getElementById("eventStartInput").value),
            endTime: toUtcIsoString(document.getElementById("eventEndInput").value),
            venue: document.getElementById("eventVenueInput").value.trim(),
            city: document.getElementById("eventCityInput").value.trim(),
            businessId: eventBusinessId.value.trim()
        };

        try {
            const response = await fetch("/api/events", {
                method: "POST",
                headers: buildAuthHeaders(),
                body: JSON.stringify(payload)
            });

            const data = await parseJson(response);
            if (!response.ok) {
                throw new Error(data?.error || data?.message || `Event creation failed with ${response.status}`);
            }

            protectedStatus.textContent = `Event created: ${data.title}. Refreshing catalog...`;
            showToast("Event created", `${data.title} was created successfully.`, "success");
            await loadEvents();
        } catch (error) {
            protectedStatus.textContent = error.message;
            showToast("Event creation failed", error.message, "error");
        }
    });
}

async function loadEvents() {
    statusBadge.textContent = "Loading events...";
    eventList.innerHTML = "";
    clearDetail();

    try {
        const response = await fetch("/api/events");
        if (!response.ok) {
            throw new Error(`Gateway returned ${response.status}`);
        }

        const payload = await response.json();
        const events = payload.events || [];

        if (events.length === 0) {
            statusBadge.textContent = "No events found";
            eventList.innerHTML = createInfoCard("No events available right now.");
            return;
        }

        statusBadge.textContent = `${events.length} events loaded`;
        eventList.innerHTML = "";

        events.forEach((event, index) => {
            const card = document.createElement("button");
            card.type = "button";
            card.className = "event-card";
            card.innerHTML = `
                <div class="event-card-top">
                    <div>
                        <h3>${escapeHtml(event.title)}</h3>
                        <p>${escapeHtml(event.description || "No description available.")}</p>
                    </div>
                    <span class="pill">${formatDate(event.startTime)}</span>
                </div>
                <div class="event-card-bottom">
                    <div class="event-meta">
                        <span><strong>Venue:</strong> ${escapeHtml(event.venue || "TBD")}</span>
                        <span><strong>Business:</strong> ${escapeHtml(event.businessId || "Unknown")}</span>
                    </div>
                    <span>View details</span>
                </div>
            `;

            card.addEventListener("click", async () => {
                highlightSelectedCard(card);
                await loadEventDetail(event.eventId);
            });

            eventList.appendChild(card);

            if (index === 0) {
                card.click();
            }
        });
    } catch (error) {
        statusBadge.textContent = "Event service unavailable";
        eventList.innerHTML = createInfoCard(
            `Could not load events from the gateway. ${escapeHtml(error.message)}`,
            true
        );
    }
}

async function loadEventDetail(eventId) {
    selectedEventId = eventId;
    exportStatus.textContent = "Loading export links...";
    showDetailShell();

    try {
        const response = await fetch(`/api/events/${eventId}`);
        if (!response.ok) {
            throw new Error(`Event detail returned ${response.status}`);
        }

        const event = await response.json();
        renderDetail(event);
        await loadExportLinks(eventId);
    } catch (error) {
        clearExportLinks();
        exportStatus.textContent = "Detail unavailable";
        detailBusiness.textContent = "Gateway to event-service";
        detailTitle.textContent = "Could not load this event";
        detailDate.textContent = "-";
        detailTime.textContent = "-";
        detailVenue.textContent = "-";
        detailId.textContent = eventId;
        detailDescription.textContent = error.message;
    }
}

async function loadExportLinks(eventId) {
    try {
        const response = await fetch(`/api/exports/events/${eventId}/link`);
        if (!response.ok) {
            throw new Error(`Calendar export returned ${response.status}`);
        }

        const links = await response.json();
        googleLink.href = links.googleLink;
        outlookLink.href = links.outlookLink;
        icsLink.href = links.calendarLink;
        googleLink.classList.remove("disabled-link");
        outlookLink.classList.remove("disabled-link");
        icsLink.classList.remove("disabled-link");
        exportStatus.textContent = "Export links ready";
    } catch (error) {
        clearExportLinks();
        exportStatus.textContent = "Export service unavailable";
    }
}

function loadSession() {
    try {
        const raw = localStorage.getItem("local-events-session");
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

function saveSession(nextSession) {
    localStorage.setItem("local-events-session", JSON.stringify(nextSession));
}

function clearSession() {
    localStorage.removeItem("local-events-session");
}

function updateSessionUi() {
    if (!session?.token) {
        sessionBadge.textContent = "Guest mode";
        sessionBadge.className = "status-badge neutral-badge";
        sessionUserId.textContent = "-";
        sessionEmail.textContent = "-";
        sessionRole.textContent = "-";
        protectedStatus.textContent = "Login as BUSINESS to unlock protected actions.";
        return;
    }

    sessionBadge.textContent = `${session.role} session`;
    sessionBadge.className = `status-badge ${session.role === "BUSINESS" ? "success-badge" : "warning-badge"}`;
    sessionUserId.textContent = session.userId ?? "-";
    sessionEmail.textContent = session.email ?? "-";
    sessionRole.textContent = session.role ?? "-";
    protectedStatus.textContent = session.role === "BUSINESS"
        ? "You can now create businesses and events through the gateway."
        : "This session is valid, but protected business actions need BUSINESS role.";
}

function buildAuthHeaders() {
    return {
        "Content-Type": "application/json",
        Authorization: `Bearer ${session.token}`
    };
}

function ensureBusinessSession() {
    if (!session?.token) {
        protectedStatus.textContent = "Please log in first.";
        return false;
    }

    if (session.role !== "BUSINESS") {
        protectedStatus.textContent = "Protected business actions require BUSINESS role.";
        return false;
    }

    return true;
}

function prefillDemoValues() {
    businessContactEmail.value = session?.email || "business@example.com";
    document.getElementById("businessName").value = "Tech Corp";
    document.getElementById("businessAddress").value = "123 Main Street";
    document.getElementById("businessCity").value = "Madrid";
    document.getElementById("businessPhone").value = "600123456";
    document.getElementById("businessDescription").value = "Business profile for testing";

    document.getElementById("eventTitleInput").value = "Tech Conference 2026";
    document.getElementById("eventDescriptionInput").value = "Annual technology conference";
    document.getElementById("eventVenueInput").value = "Convention Center";
    document.getElementById("eventCityInput").value = "Madrid";
    document.getElementById("eventStartInput").value = "2026-06-15T09:00";
    document.getElementById("eventEndInput").value = "2026-06-15T17:00";
}

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

function toUtcIsoString(value) {
    return new Date(value).toISOString();
}

function showToast(title, message, tone = "success") {
    const toast = document.createElement("article");
    toast.className = `toast ${tone === "error" ? "toast-error" : "toast-success"}`;
    toast.innerHTML = `
        <strong>${escapeHtml(title)}</strong>
        <span>${escapeHtml(message)}</span>
    `;

    toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 5000);
}

let map = null;

function renderDetail(event) {
    detailBusiness.textContent = event.business?.name
        ? `Hosted by ${event.business.name}`
        : `Business ID ${event.businessId || "not available"}`;
    detailTitle.textContent = event.title || "Untitled event";
    detailDate.textContent = formatDate(event.startTime);
    detailTime.textContent = `${formatTime(event.startTime)} - ${formatTime(event.endTime)}`;
    detailVenue.textContent = event.venue || "TBD";
    detailId.textContent = event.eventId || selectedEventId || "-";
    detailDescription.textContent = event.description || "No description available.";

    const locationParts = [
        event.venue,
        event.city,
        "Netherlands"
    ].filter(Boolean);

    const address = locationParts.join(", ");

    fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
        .then(res => res.json())
        .then(data => {
            if (!data.length) {
                console.warn("Location not found");
                return;
            }

            const lat = parseFloat(data[0].lat);
            const lon = parseFloat(data[0].lon);

            if (map) {
                map.remove();
            }

            map = L.map('map').setView([lat, lon], 16);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);

            L.marker([lat, lon])
                .addTo(map)
                .bindPopup(address)
                .openPopup();
        })
        .catch(err => console.error("Map error:", err));
}

function showDetailShell() {
    emptyState.classList.add("hidden");
    eventDetail.classList.remove("hidden");
}

function clearDetail() {
    selectedEventId = null;
    emptyState.classList.remove("hidden");
    eventDetail.classList.add("hidden");
    clearExportLinks();
    exportStatus.textContent = "Ready";
}

function clearExportLinks() {
    [googleLink, outlookLink, icsLink].forEach((anchor) => {
        anchor.href = "#";
        anchor.classList.add("disabled-link");
    });
}

function highlightSelectedCard(activeCard) {
    document.querySelectorAll(".event-card").forEach((card) => {
        card.classList.toggle("selected", card === activeCard);
    });
}

function createInfoCard(message, isError = false) {
    return `
        <article class="detail-card ${isError ? "error-card" : ""}">
            <h3>${isError ? "Something went wrong" : "Heads up"}</h3>
            <p>${message}</p>
        </article>
    `;
}

function formatDate(value) {
    if (!value) {
        return "Unknown date";
    }

    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    }).format(new Date(value));
}

function formatTime(value) {
    if (!value) {
        return "--:--";
    }

    return new Intl.DateTimeFormat("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
        timeZone: "UTC"
    }).format(new Date(value)) + " UTC";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}
