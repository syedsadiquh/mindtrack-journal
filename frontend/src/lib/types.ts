// API types matching MindTrack Journal backend

export type SentimentLabel = "POSITIVE" | "NEGATIVE" | "NEUTRAL";
export type BlockType =
  | "TEXT"
  | "IMAGE"
  | "VIDEO"
  | "AUDIO"
  | "QUOTE"
  | "HEADING";
export type PlanTier = "FREE" | "PRO" | "ENTERPRISE";
export type AnalyticsPeriod = "DAILY" | "WEEKLY" | "MONTHLY";
export type UserRole = "USER" | "ADMIN" | "SYS_ADMIN";

export interface BaseResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface AuthTokens {
  access_token: string;
  refresh_token?: string;
  expires_in: string;
  defaultTenantSlug: string;
}

export interface Tenant {
  tenantId: string;
  tenantSlug: string;
  tenantName: string;
  active?: boolean;
  planDisplayName?: string;
  planTier?: PlanTier;
}

export interface UserProfile {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  defaultTenant: Tenant;
  active: boolean;
  avatarUrl?: string;
  countryCode?: string;
  phone?: string;
  timezone?: string;
  address?: string;
  roles?: UserRole[];
}

export interface Tag {
  id: string;
  name: string;
  color: string;
}

export interface SentimentData {
  sentimentLabel: SentimentLabel;
  sentimentScore: number;
  sentimentScores?: Record<string, number>;
  dominantEmotion?: string;
  emotionVector?: Record<string, number>;
  analysedAt?: string;
}

export interface JournalBlock {
  id: string;
  parentBlockId?: string | null;
  type: BlockType;
  orderIndex: number;
  content: { text?: string; url?: string; caption?: string } & Record<
    string,
    unknown
  >;
  metadata?: Record<string, unknown>;
  childBlocks?: JournalBlock[];
  createdAt?: string;
  updatedAt?: string;
}

export interface JournalPageDetail {
  id: string;
  tenantId: string;
  userId: string;
  title: string;
  description?: string;
  coverImageUrl?: string;
  entryDate: string;
  isPrivate: boolean;
  tags: Tag[];
  blocks: JournalBlock[];
  sentiment?: SentimentData;
  aiEnrichment?: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

export interface JournalPageSummary {
  id: string;
  tenantId: string;
  userId: string;
  title: string;
  description?: string;
  coverImageUrl?: string;
  entryDate: string;
  isPrivate: boolean;
  tags: Tag[];
  blockCount: number;
  sentimentLabel?: SentimentLabel;
  sentimentScore?: number;
  dominantEmotion?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface AnalyticsSummary {
  totalEntries: number;
  avgSentimentScore: number;
  positiveCount: number;
  negativeCount: number;
  neutralCount: number;
  mostFrequentEmotion: string;
  currentStreak: number;
  longestStreak: number;
}

export interface SentimentTrendPoint {
  period: string;
  entryCount: number;
  avgSentimentScore: number;
  sentimentLabel: SentimentLabel;
  dominantEmotion: string;
}

export interface MoodCalendarEntry {
  date: string;
  entryCount: number;
  sentimentScore: number;
  sentimentLabel: SentimentLabel;
  dominantEmotion: string;
}

export interface EmotionDistribution {
  emotion: string;
  count: number;
  percentage: number;
}

export interface WritingStreak {
  currentStreak: number;
  longestStreak: number;
  lastEntryDate: string;
}

export interface AnalyticsFeedItem {
  entryDate: string;
  sentimentLabel: SentimentLabel;
  sentimentScore: number;
  dominantEmotion: string;
}

export interface Plan {
  id: string;
  tier: PlanTier;
  displayName: string;
  description?: string;
  maxPages: number;
  maxBlocksPerPage: number;
  maxTags: number;
  maxMembers: number;
  aiEnrichmentEnabled: boolean;
  sentimentAnalysisEnabled: boolean;
  sharingEnabled: boolean;
  exportEnabled: boolean;
  priceMonthly: number;
  priceYearly: number;
  currency: string;
  active: boolean;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface CreateJournalPageRequest {
  tenantId: string;
  title: string;
  description?: string;
  coverImageUrl?: string;
  entryDate: string;
  isPrivate?: boolean;
  tagIds?: string[];
  blocks?: Array<{
    type: BlockType;
    parentBlockId?: string | null;
    orderIndex: number;
    content: Record<string, unknown>;
    metadata?: Record<string, unknown>;
  }>;
}
