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

let selectedEventId = null;

refreshButton.addEventListener("click", () => {
    loadEvents();
});

document.addEventListener("DOMContentLoaded", () => {
    loadEvents();
});

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
