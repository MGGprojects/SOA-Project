import {
    addFavorite,
    createBusiness,
    createEvent,
    createUserProfile,
    deleteEvent,
    getEvent,
    getEvents,
    getExportLinks,
    getFavoriteIds,
    getUserProfile,
    getVenueAvailability,
    loginAuth,
    registerAuth,
    removeFavorite,
    updateEvent,
    updateUserProfile
} from "./api.js";
import { clearSession, getStoredProfile, loadSession, saveSession, saveStoredProfile } from "./storage.js";
import {
    clearDetail,
    clearExportLinks,
    createInfoCard,
    elements,
    renderDetail,
    renderAvailability,
    renderEventList,
    renderFavoriteEvents,
    setExportLinks,
    showAuthTab,
    showToast,
    showFlowTab,
    updateProfileUi,
    updateSessionUi
} from "./ui.js";
import { overlaps, toUtcIsoString } from "./utils.js";

const state = {
    session: loadSession(),
    profile: null,
    selectedEventId: null,
    events: [],
    favoriteIds: new Set(),
    favoriteEvents: []
};

document.addEventListener("DOMContentLoaded", () => {
    hydrateProfile();
    wireAuthForms();
    wireProfileForm();
    wireProtectedForms();
    wireAvailabilityForm();
    wireFilters();
    wireFavoriteButton();
    wireAuthTabs();
    wireFlowTabs();
    prefillDemoValues();
    showAuthTab("register");
    showFlowTab("customer");
    updateSessionUi(state.session);
    updateProfileUi(state.profile);
    void refreshFavoriteState();
    void loadEvents();
});

function hydrateProfile() {
    state.profile = state.session?.userId ? getStoredProfile(state.session.userId) : null;
}

function wireAuthForms() {
    elements.registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        elements.authStatus.textContent = "Creating auth account...";

        const payload = {
            email: document.getElementById("registerEmail").value.trim(),
            password: document.getElementById("registerPassword").value,
            role: document.getElementById("registerRole").value
        };

        try {
            const data = await registerAuth(payload);
            elements.authStatus.textContent = `Account created for ${data.email}. You can log in now.`;
            showToast("Account created", `Registered ${data.email} as ${data.role}.`, "success");
            document.getElementById("loginEmail").value = payload.email;
            document.getElementById("loginPassword").value = payload.password;
            if (payload.role === "BUSINESS") {
                elements.businessContactEmail.value = payload.email;
            }
            elements.registerForm.reset();
        } catch (error) {
            elements.authStatus.textContent = error.message;
            showToast("Register failed", error.message, "error");
        }
    });

    elements.loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        elements.authStatus.textContent = "Signing in...";

        const payload = {
            email: document.getElementById("loginEmail").value.trim(),
            password: document.getElementById("loginPassword").value
        };

        try {
            const data = await loginAuth(payload);
            state.session = {
                token: data.token,
                userId: data.userId,
                role: data.role,
                email: payload.email
            };
            saveSession(state.session);
            hydrateProfile();
            if (state.profile?.userId) {
                await reloadProfile();
            }
            updateSessionUi(state.session);
            updateProfileUi(state.profile);
            elements.authStatus.textContent = `Logged in as ${payload.email}.`;
            showToast("Login successful", `Session active for ${payload.email}.`, "success");
            if (state.session.role === "BUSINESS") {
                elements.businessContactEmail.value = state.session.email;
            }
            await refreshFavoriteState();
        } catch (error) {
            elements.authStatus.textContent = error.message;
            showToast("Login failed", error.message, "error");
        }
    });

    elements.logoutButton.addEventListener("click", async () => {
        clearSession();
        state.session = null;
        state.profile = null;
        state.favoriteIds = new Set();
        state.favoriteEvents = [];
        updateSessionUi(state.session);
        updateProfileUi(state.profile);
        elements.authStatus.textContent = "Session cleared in the browser.";
        showToast("Logged out", "The local browser session was cleared.", "success");
        renderFavoriteEvents([], selectEventById);
        await loadEvents();
    });
}

function wireProfileForm() {
    elements.profileForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!state.session?.token) {
            elements.profileStatus.textContent = "Please log in first.";
            return;
        }

        const payload = {
            firstName: document.getElementById("profileFirstName").value.trim(),
            lastName: document.getElementById("profileLastName").value.trim()
        };

        try {
            const profile = await createUserProfile(payload, state.session);
            state.profile = profile;
            saveStoredProfile(state.session.userId, profile);
            updateProfileUi(profile);
            showToast("Profile created", `${profile.firstName} ${profile.lastName} can now save favorites.`, "success");
            await refreshFavoriteState();
        } catch (error) {
            elements.profileStatus.textContent = error.message;
            showToast("Profile creation failed", error.message, "error");
        }
    });

    elements.profileUpdateForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!state.session?.token || !state.profile?.userId) {
            elements.profileStatus.textContent = "Create or load a profile first.";
            return;
        }

        const payload = {
            firstName: elements.profileUpdateFirstName.value.trim(),
            lastName: elements.profileUpdateLastName.value.trim()
        };

        try {
            const profile = await updateUserProfile(state.profile.userId, payload, state.session);
            state.profile = profile;
            saveStoredProfile(state.session.userId, profile);
            updateProfileUi(profile);
            elements.profileStatus.textContent = "Profile updated successfully.";
            showToast("Profile updated", "Your profile changes were saved.", "success");
        } catch (error) {
            elements.profileStatus.textContent = error.message;
            showToast("Profile update failed", error.message, "error");
        }
    });

    elements.loadProfileButton.addEventListener("click", async () => {
        await reloadProfile(true);
    });
}

function wireProtectedForms() {
    elements.businessForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!ensureBusinessSession()) {
            return;
        }

        elements.protectedStatus.textContent = "Creating business...";

        const payload = {
            name: document.getElementById("businessName").value.trim(),
            address: document.getElementById("businessAddress").value.trim(),
            city: document.getElementById("businessCity").value.trim(),
            contactEmail: elements.businessContactEmail.value.trim(),
            phone: document.getElementById("businessPhone").value.trim(),
            description: document.getElementById("businessDescription").value.trim()
        };

        try {
            const data = await createBusiness(payload, state.session);
            elements.eventBusinessId.value = data.businessId;
            elements.protectedStatus.textContent = `Business created with id ${data.businessId}.`;
            showToast("Business created", `${data.name} is ready with business ID ${data.businessId}.`, "success");
        } catch (error) {
            elements.protectedStatus.textContent = error.message;
            showToast("Business creation failed", error.message, "error");
        }
    });

    elements.eventForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!ensureBusinessSession()) {
            return;
        }

        const payload = {
            title: document.getElementById("eventTitleInput").value.trim(),
            description: document.getElementById("eventDescriptionInput").value.trim(),
            startTime: toUtcIsoString(document.getElementById("eventStartInput").value),
            endTime: toUtcIsoString(document.getElementById("eventEndInput").value),
            venue: document.getElementById("eventVenueInput").value.trim(),
            city: document.getElementById("eventCityInput").value.trim(),
            businessId: elements.eventBusinessId.value.trim()
        };

        const competition = await findCompetingEvents(payload);
        if (competition.length) {
            const conflictNames = competition.slice(0, 3).map((item) => item.title).join(", ");
            elements.competitionStatus.textContent = `Competition warning: ${competition.length} event(s) already exist at ${payload.venue} around that time.`;
            showToast("Competition warning", `${competition.length} competing event(s): ${conflictNames}.`, "error");
            const shouldContinue = window.confirm(
                `There are already ${competition.length} event(s) at ${payload.venue} around that time.\n\nExamples: ${conflictNames}\n\nDo you still want to publish your event?`
            );
            if (!shouldContinue) {
                elements.protectedStatus.textContent = "Event creation cancelled after competition warning.";
                return;
            }
        } else {
            elements.competitionStatus.textContent = "No competing event found for the chosen venue and time.";
        }

        elements.protectedStatus.textContent = "Creating event...";

        try {
            const data = await createEvent(payload, state.session);
            elements.protectedStatus.textContent = `Event created: ${data.title}. Refreshing catalog...`;
            showToast("Event created", `${data.title} was created successfully.`, "success");
            await loadEvents();
        } catch (error) {
            elements.protectedStatus.textContent = error.message;
            showToast("Event creation failed", error.message, "error");
        }
    });

    elements.eventUpdateForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!ensureBusinessSession() || !state.selectedEventId) {
            elements.manageEventStatus.textContent = "Select an event and log in as BUSINESS first.";
            return;
        }

        const payload = {
            title: elements.updateEventTitleInput.value.trim(),
            description: elements.updateEventDescriptionInput.value.trim(),
            startTime: elements.updateEventStartInput.value ? toUtcIsoString(elements.updateEventStartInput.value) : null,
            endTime: elements.updateEventEndInput.value ? toUtcIsoString(elements.updateEventEndInput.value) : null,
            venue: elements.updateEventVenueInput.value.trim()
        };

        Object.keys(payload).forEach((key) => {
            if (payload[key] === "" || payload[key] === null) {
                delete payload[key];
            }
        });

        try {
            elements.manageEventStatus.textContent = "Updating event...";
            const updated = await updateEvent(state.selectedEventId, payload, state.session);
            elements.manageEventStatus.textContent = `Event updated: ${updated.title}.`;
            showToast("Event updated", "Selected event was updated successfully.", "success");
            await loadEvents();
            await selectEventById(state.selectedEventId);
        } catch (error) {
            elements.manageEventStatus.textContent = error.message;
            showToast("Event update failed", error.message, "error");
        }
    });

    elements.deleteEventButton.addEventListener("click", async () => {
        if (!ensureBusinessSession() || !state.selectedEventId) {
            elements.manageEventStatus.textContent = "Select an event and log in as BUSINESS first.";
            return;
        }

        const shouldDelete = window.confirm("Delete the selected event? This action cannot be undone.");
        if (!shouldDelete) {
            return;
        }

        try {
            elements.manageEventStatus.textContent = "Deleting event...";
            await deleteEvent(state.selectedEventId, state.session);
            showToast("Event deleted", "Selected event was deleted successfully.", "success");
            state.selectedEventId = null;
            await loadEvents();
        } catch (error) {
            elements.manageEventStatus.textContent = error.message;
            showToast("Event deletion failed", error.message, "error");
        }
    });
}

function wireAvailabilityForm() {
    elements.availabilityForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const venue = elements.availabilityVenueInput.value.trim();
        const date = elements.availabilityDateInput.value;

        if (!venue) {
            elements.availabilityStatus.textContent = "Please provide a venue name.";
            return;
        }

        try {
            elements.availabilityStatus.textContent = "Checking venue availability...";
            const response = await getVenueAvailability(venue, date);
            renderAvailability(response);
            elements.availabilityStatus.textContent = `Availability loaded for ${response.venue || venue}.`;
        } catch (error) {
            elements.availabilityStatus.textContent = error.message;
            elements.availabilityList.innerHTML = createInfoCard(`Could not load venue availability. ${error.message}`, true);
        }
    });
}

function wireFilters() {
    elements.refreshButton.addEventListener("click", () => loadEvents());
    elements.filterForm.addEventListener("submit", (event) => {
        event.preventDefault();
        void loadEvents();
    });
    elements.clearFiltersButton.addEventListener("click", () => {
        elements.filterForm.reset();
        void loadEvents();
    });
}

function wireFavoriteButton() {
    elements.favoriteButton.addEventListener("click", async () => {
        if (!state.selectedEventId) {
            return;
        }

        if (!state.session?.token) {
            showToast("Login needed", "Please log in before managing favorites.", "error");
            return;
        }

        if (!state.profile?.userId) {
            showToast("Profile needed", "Create a user profile first to save favorites.", "error");
            return;
        }

        try {
            if (state.favoriteIds.has(state.selectedEventId)) {
                await removeFavorite(state.profile.userId, state.selectedEventId, state.session);
                showToast("Favorite removed", "The event was removed from your favorites.", "success");
            } else {
                await addFavorite(state.profile.userId, state.selectedEventId, state.session);
                showToast("Favorite saved", "The event was added to your favorites.", "success");
            }

            await refreshFavoriteState();
            await loadEvents();
            if (state.selectedEventId) {
                await selectEventById(state.selectedEventId);
            }
        } catch (error) {
            showToast("Favorite action failed", error.message, "error");
        }
    });
}

function wireAuthTabs() {
    elements.registerTabButton.addEventListener("click", () => showAuthTab("register"));
    elements.loginTabButton.addEventListener("click", () => showAuthTab("login"));
}

function wireFlowTabs() {
    elements.customerTabButton.addEventListener("click", () => showFlowTab("customer"));
    elements.businessTabButton.addEventListener("click", () => showFlowTab("business"));
}

async function loadEvents() {
    elements.statusBadge.textContent = "Loading events...";
    elements.eventList.innerHTML = "";
    clearDetail();

    try {
        const filters = {
            city: elements.filterCity.value.trim(),
            date: elements.filterDate.value,
            businessId: elements.filterBusinessId.value.trim(),
            limit: 100
        };

        const payload = await getEvents(filters);
        const keyword = elements.filterKeyword.value.trim().toLowerCase();
        const events = (payload.events || []).filter((event) => {
            if (!keyword) {
                return true;
            }

            return [event.title, event.description]
                .filter(Boolean)
                .some((value) => value.toLowerCase().includes(keyword));
        });

        state.events = events;
        elements.statusBadge.textContent = `${events.length} events loaded`;
        renderEventList(events, state.selectedEventId, state.favoriteIds, selectEventById);

        const eventToShow = events.find((item) => item.eventId === state.selectedEventId) || events[0];
        if (eventToShow) {
            await selectEventById(eventToShow.eventId);
        } else {
            elements.eventList.innerHTML = createInfoCard("No events match the current filters.");
        }
    } catch (error) {
        elements.statusBadge.textContent = "Event service unavailable";
        elements.eventList.innerHTML = createInfoCard(`Could not load events from the gateway. ${error.message}`, true);
    }
}

async function selectEventById(eventId) {
    state.selectedEventId = eventId;

    try {
        const event = await getEvent(eventId);
        renderDetail(event, state.favoriteIds.has(eventId));
        try {
            const links = await getExportLinks(eventId);
            setExportLinks(links);
        } catch {
            clearExportLinks();
            elements.exportStatus.textContent = "Export service unavailable";
        }

        renderEventList(state.events, state.selectedEventId, state.favoriteIds, selectEventById);
    } catch (error) {
        clearExportLinks();
        elements.exportStatus.textContent = "Detail unavailable";
        elements.detailBusiness.textContent = "Gateway to event-service";
        elements.detailTitle.textContent = "Could not load this event";
        elements.detailDate.textContent = "-";
        elements.detailTime.textContent = "-";
        elements.detailVenue.textContent = "-";
        elements.detailId.textContent = eventId;
        elements.detailDescription.textContent = error.message;
        elements.detailMap.src = "";
    }
}

async function refreshFavoriteState() {
    if (!state.session?.token || !state.profile?.userId) {
        state.favoriteIds = new Set();
        state.favoriteEvents = [];
        renderFavoriteEvents([], selectEventById);
        return;
    }

    try {
        const response = await getFavoriteIds(state.profile.userId, state.session);
        const ids = response.events || [];
        state.favoriteIds = new Set(ids);
        state.favoriteEvents = (await Promise.all(ids.map(async (eventId) => {
            try {
                return await getEvent(eventId);
            } catch {
                return null;
            }
        }))).filter(Boolean);
        renderFavoriteEvents(state.favoriteEvents, selectEventById);
    } catch (error) {
        elements.favoritesList.innerHTML = createInfoCard(`Could not load favorites. ${error.message}`, true);
    }
}

async function reloadProfile(showFeedback = false) {
    if (!state.session?.token || !state.profile?.userId) {
        return;
    }

    try {
        const profile = await getUserProfile(state.profile.userId, state.session);
        state.profile = profile;
        saveStoredProfile(state.session.userId, profile);
        updateProfileUi(profile);
        if (showFeedback) {
            elements.profileStatus.textContent = "Profile reloaded from user-service.";
            showToast("Profile loaded", "Latest profile data was fetched successfully.", "success");
        }
    } catch (error) {
        if (showFeedback) {
            elements.profileStatus.textContent = error.message;
            showToast("Profile load failed", error.message, "error");
        }
    }
}

async function findCompetingEvents(payload) {
    if (!payload.city || !payload.venue || !payload.startTime || !payload.endTime) {
        return [];
    }

    try {
        const sameDay = await getEvents({
            city: payload.city,
            date: payload.startTime.slice(0, 10),
            limit: 100
        });

        return (sameDay.events || []).filter((event) =>
            event.venue?.trim().toLowerCase() === payload.venue.trim().toLowerCase()
            && overlaps(event.startTime, event.endTime, payload.startTime, payload.endTime)
        );
    } catch {
        return [];
    }
}

function ensureBusinessSession() {
    if (!state.session?.token) {
        elements.protectedStatus.textContent = "Please log in first.";
        return false;
    }

    if (state.session.role !== "BUSINESS") {
        elements.protectedStatus.textContent = "Protected business actions require BUSINESS role.";
        return false;
    }

    return true;
}

function prefillDemoValues() {
    elements.businessContactEmail.value = state.session?.email || "business@example.com";
    document.getElementById("businessName").value = "Tech Corp";
    document.getElementById("businessAddress").value = "123 Main Street";
    document.getElementById("businessCity").value = "Madrid";
    document.getElementById("businessPhone").value = "600123456";
    document.getElementById("businessDescription").value = "Business profile for testing";
    document.getElementById("profileFirstName").value = "Jane";
    document.getElementById("profileLastName").value = "Doe";
    document.getElementById("eventTitleInput").value = "Tech Conference 2026";
    document.getElementById("eventDescriptionInput").value = "Annual technology conference";
    document.getElementById("eventVenueInput").value = "Convention Center";
    document.getElementById("eventCityInput").value = "Madrid";
    document.getElementById("eventStartInput").value = "2026-06-15T09:00";
    document.getElementById("eventEndInput").value = "2026-06-15T17:00";
}
