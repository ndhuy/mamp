// Types mirroring the backend DTOs.

export type Role = 'USER' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'DISABLED'
export type MediaTypeValue = 'PHOTO' | 'FOOTAGE'
export type ContentUsageType = 'COMMERCIAL' | 'EDITORIAL'
export type WorkflowStatus =
  | 'DRAFT' | 'EDITING' | 'METADATA_PENDING' | 'READY' | 'UPLOADING' | 'COMPLETED' | 'ARCHIVED'
export type SubmissionStatus =
  | 'NOT_SUBMITTED' | 'SUBMITTED' | 'IN_REVIEW' | 'ACCEPTED' | 'REJECTED'
  | 'RESUBMIT_REQUIRED' | 'RESUBMITTED' | 'REMOVED'
export type DeviceType =
  | 'INTERCHANGEABLE_LENS' | 'FIXED_LENS' | 'SMARTPHONE' | 'DRONE'
  | 'ACTION_CAMERA' | 'CAMERA_360' | 'OTHER'

export interface User {
  id: string
  email: string
  username: string
  role: Role
  status: UserStatus
}

export interface UserAdmin {
  id: string
  email: string
  username: string
  role: Role
  status: UserStatus
  createdAt: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
  user: User
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface MediaSummary {
  id: string
  code: string
  ownerId: string
  title: string
  mediaType: MediaTypeValue
  thumbnailKey: string | null
  workflowStatus: WorkflowStatus
  contentUsageType: ContentUsageType | null
  conceptCount: number
  keywordCount: number
  createdAt: string
  updatedAt: string
}

export interface StockSite {
  id: string
  name: string
  website: string | null
  dashboardUrl: string | null
  notes: string | null
  displayOrder: number
  categoriesRequired: number
  active: boolean
}

export interface CaptureDevice {
  id: string
  brand: string
  model: string
  deviceType: DeviceType
  mount: string | null
  serialNumber: string | null
  notes: string | null
  active: boolean
}

export interface RejectionCategory {
  id: string
  name: string
  active: boolean
}

export interface DashboardCount {
  label: string
  value: number
}

export interface SiteReview {
  site: string
  accepted: number
  rejected: number
}

export interface Dashboard {
  systemWide: boolean
  totalMedia: number
  photos: number
  footage: number
  readyForUpload: number
  missingKeywords: number
  totalSubmissions: number
  submitted: number
  inReview: number
  accepted: number
  rejected: number
  acceptanceRate: number
  mediaByStatus: DashboardCount[]
  submissionsByStatus: DashboardCount[]
  mediaByMonth: DashboardCount[]
  mediaByDevice: DashboardCount[]
  acceptedRejectedBySite: SiteReview[]
}

export type LensTypeValue = 'PRIME' | 'ZOOM' | 'MACRO' | 'TELEPHOTO' | 'OTHER'

export interface CaptureDevicePayload {
  brand: string
  model: string
  deviceType: DeviceType
  mount?: string | null
  serialNumber?: string | null
  notes?: string | null
}

export interface LensPayload {
  brand: string
  model: string
  mount?: string | null
  lensType?: LensTypeValue | null
  minFocalLength?: number | null
  maxFocalLength?: number | null
  maxAperture?: string | null
  notes?: string | null
}

export interface StockSitePayload {
  name: string
  website?: string | null
  dashboardUrl?: string | null
  notes?: string | null
  displayOrder?: number | null
  categoriesRequired?: number | null
}

export interface SiteCategoryPayload {
  name: string
  parentId?: string | null
}

export interface RejectionCategoryPayload {
  name: string
}

export type StorageTypeValue =
  | 'LOCAL_DISK' | 'EXTERNAL_DRIVE' | 'NAS' | 'GOOGLE_DRIVE' | 'DROPBOX' | 'ONEDRIVE' | 'OTHER'

export interface NamedRef {
  id: string
  name: string
}

export interface Concept {
  id: string
  name: string
  description: string | null
  active: boolean
}

export interface Lens {
  id: string
  brand: string
  model: string
  mount: string | null
  lensType: LensTypeValue | null
  minFocalLength: number | null
  maxFocalLength: number | null
  maxAperture: string | null
  notes: string | null
  active: boolean
}

export interface MediaDetail {
  id: string
  code: string
  ownerId: string
  title: string
  mediaType: MediaTypeValue
  thumbnailKey: string | null
  description: string | null
  notes: string | null
  captureDate: string | null
  location: string | null
  captureDevice: NamedRef | null
  lens: NamedRef | null
  contentUsageType: ContentUsageType | null
  workflowStatus: WorkflowStatus
  originalFilePath: string | null
  exportFilePath: string | null
  storageType: StorageTypeValue | null
  aiGenerated: boolean
  editorialCaption: string | null
  eventDate: string | null
  editorialLocation: string | null
  eventSubjectName: string | null
  editorialNotes: string | null
  concepts: NamedRef[]
  keywords: string[]
  deleted: boolean
  createdAt: string
  updatedAt: string
}

export interface SiteCategory {
  id: string
  stockSiteId: string
  name: string
  parentId: string | null
  parentName: string | null
  active: boolean
}

export interface Submission {
  id: string
  mediaId: string
  stockSiteId: string
  stockSiteName: string
  status: SubmissionStatus
  primaryCategory: NamedRef | null
  secondaryCategory: NamedRef | null
  contributorAssetId: string | null
  assetUrl: string | null
  submittedDate: string | null
  reviewedDate: string | null
  rejectionCategory: NamedRef | null
  rejectionDetail: string | null
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface SubmissionUpdatePayload {
  status: SubmissionStatus
  primaryCategoryId?: string | null
  secondaryCategoryId?: string | null
  contributorAssetId?: string | null
  assetUrl?: string | null
  submittedDate?: string | null
  reviewedDate?: string | null
  rejectionCategoryId?: string | null
  rejectionDetail?: string | null
  notes?: string | null
}

export interface MediaRequestPayload {
  title: string
  mediaType: MediaTypeValue
  thumbnailKey?: string | null
  description?: string | null
  notes?: string | null
  captureDate?: string | null
  location?: string | null
  captureDeviceId?: string | null
  lensId?: string | null
  contentUsageType?: ContentUsageType | null
  workflowStatus?: WorkflowStatus | null
  originalFilePath?: string | null
  exportFilePath?: string | null
  storageType?: StorageTypeValue | null
  aiGenerated?: boolean
  editorialCaption?: string | null
  eventDate?: string | null
  editorialLocation?: string | null
  eventSubjectName?: string | null
  editorialNotes?: string | null
  conceptIds?: string[]
  keywords?: string[]
}
