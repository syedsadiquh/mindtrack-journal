import { api } from "./api-client";
import type {
  AnalyticsSummary,
  AnalyticsFeedItem,
  CreateJournalPageRequest,
  EmotionDistribution,
  JournalPageDetail,
  JournalPageSummary,
  MoodCalendarEntry,
  PageResponse,
  Plan,
  RegisterRequest,
  SentimentData,
  SentimentTrendPoint,
  Tag,
  UserProfile,
  WritingStreak,
} from "./types";

// Auth
export const authApi = {
  register: (body: RegisterRequest) =>
    api<{ userId: string; username: string; email: string }>(
      "/api/v1/auth/register",
      {
        method: "POST",
        body,
      },
    ),
  registerAdmin: (body: RegisterRequest) =>
    api<null>("/api/v1/admin/auth/register", { method: "POST", body }),
  deleteUser: (id: string) =>
    api<null>(`/api/v1/auth/delete/${id}`, { method: "DELETE" }),
};

// Profile
export const userApi = {
  me: () => api<UserProfile>("/api/v1/users/me"),
  update: (body: Partial<UserProfile>) =>
    api<UserProfile>("/api/v1/users/me", { method: "PATCH", body }),
};

// Plans
export const planApi = {
  listPublic: () => api<Plan[]>("/api/v1/plans"),
  get: (id: string) => api<Plan>(`/api/v1/plans/${id}`),
  // admin
  listAdmin: () => api<Plan[]>("/api/v1/admin/plans"),
  getAdmin: (id: string) => api<Plan>(`/api/v1/admin/plans/${id}`),
  create: (body: Partial<Plan>) =>
    api<Plan>("/api/v1/admin/plans", { method: "POST", body }),
  update: (id: string, body: Partial<Plan>) =>
    api<Plan>(`/api/v1/admin/plans/${id}`, { method: "PUT", body }),
  deactivate: (id: string) =>
    api<null>(`/api/v1/admin/plans/${id}/deactivate`, { method: "PATCH" }),
  reactivate: (id: string) =>
    api<null>(`/api/v1/admin/plans/${id}/reactivate`, { method: "PATCH" }),
};

// Journals
export const journalApi = {
  create: (body: CreateJournalPageRequest) =>
    api<JournalPageDetail>("/api/v1/journals/pages", { method: "POST", body }),
  get: (id: string) => api<JournalPageDetail>(`/api/v1/journals/pages/${id}`),
  list: (
    params: {
      from?: string;
      to?: string;
      page?: number;
      size?: number;
      sort?: string;
    } = {},
  ) =>
    api<PageResponse<JournalPageSummary>>("/api/v1/journals/pages", {
      query: params,
    }),
  updateMeta: (
    id: string,
    body: {
      title?: string;
      description?: string;
      coverImageUrl?: string;
      isPrivate?: boolean;
    },
  ) =>
    api<JournalPageDetail>(`/api/v1/journals/pages/${id}`, {
      method: "PUT",
      body,
    }),
  addTag: (pageId: string, tagId: string) =>
    api<null>(`/api/v1/journals/pages/${pageId}/tags/${tagId}`, {
      method: "POST",
    }),
  removeTag: (pageId: string, tagId: string) =>
    api<null>(`/api/v1/journals/pages/${pageId}/tags/${tagId}`, {
      method: "DELETE",
    }),
  analyticsFeed: (params: { from?: string; to?: string } = {}) =>
    api<AnalyticsFeedItem[]>("/api/v1/journals/pages/analytics-feed", {
      query: params,
    }),
  analyze: (id: string) =>
    api<SentimentData>(`/api/v1/journals/pages/${id}/analyze`, {
      method: "POST",
    }),
};

// Tags
export const tagApi = {
  list: (tenantId: string) =>
    api<Tag[]>("/api/v1/journals/tags", { query: { tenantId } }),
  create: (body: { tenantId: string; name: string; color?: string }) =>
    api<Tag>("/api/v1/journals/tags", { method: "POST", body }),
  update: (
    id: string,
    tenantId: string,
    body: { name?: string; color?: string },
  ) =>
    api<Tag>(`/api/v1/journals/tags/${id}`, {
      method: "PUT",
      query: { tenantId },
      body,
    }),
  delete: (id: string, tenantId: string) =>
    api<null>(`/api/v1/journals/tags/${id}`, {
      method: "DELETE",
      query: { tenantId },
    }),
};

// Analytics
export const analyticsApi = {
  refresh: () => api<null>("/api/v1/analytics/refresh", { method: "POST" }),
  refreshStreak: () =>
    api<null>("/api/v1/analytics/refresh/streak", { method: "POST" }),
  summary: () => api<AnalyticsSummary>("/api/v1/analytics/summary"),
  trends: (
    params: {
      period?: "DAILY" | "WEEKLY" | "MONTHLY";
      from?: string;
      to?: string;
    } = {},
  ) =>
    api<SentimentTrendPoint[]>("/api/v1/analytics/sentiment/trends", {
      query: params,
    }),
  moodCalendar: (params: { year?: number; month?: number } = {}) =>
    api<MoodCalendarEntry[]>("/api/v1/analytics/mood/calendar", {
      query: params,
    }),
  emotions: (params: { from?: string; to?: string } = {}) =>
    api<EmotionDistribution[]>("/api/v1/analytics/emotions/distribution", {
      query: params,
    }),
  streak: () => api<WritingStreak>("/api/v1/analytics/writing/streak"),
};
