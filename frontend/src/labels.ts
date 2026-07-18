import type { DeviceType, LensTypeValue, StorageTypeValue, SubmissionStatus, WorkflowStatus } from './api/types'

export const DEVICE_TYPE_LABELS: Record<DeviceType, string> = {
  INTERCHANGEABLE_LENS: 'Interchangeable Lens',
  FIXED_LENS: 'Fixed Lens',
  SMARTPHONE: 'Smartphone',
  DRONE: 'Drone',
  ACTION_CAMERA: 'Action Camera',
  CAMERA_360: '360 Camera',
  OTHER: 'Other',
}
export const DEVICE_TYPE_OPTIONS = Object.keys(DEVICE_TYPE_LABELS) as DeviceType[]

export const LENS_TYPE_LABELS: Record<LensTypeValue, string> = {
  PRIME: 'Prime',
  ZOOM: 'Zoom',
  MACRO: 'Macro',
  TELEPHOTO: 'Telephoto',
  OTHER: 'Other',
}
export const LENS_TYPE_OPTIONS = Object.keys(LENS_TYPE_LABELS) as LensTypeValue[]

export const WORKFLOW_STATUS_OPTIONS: WorkflowStatus[] = [
  'DRAFT', 'EDITING', 'METADATA_PENDING', 'READY', 'UPLOADING', 'COMPLETED', 'ARCHIVED',
]

export const STORAGE_TYPE_LABELS: Record<StorageTypeValue, string> = {
  LOCAL_DISK: 'Local Disk',
  EXTERNAL_DRIVE: 'External Drive',
  NAS: 'NAS',
  GOOGLE_DRIVE: 'Google Drive',
  DROPBOX: 'Dropbox',
  ONEDRIVE: 'OneDrive',
  OTHER: 'Other',
}

export const STORAGE_TYPE_OPTIONS = Object.keys(STORAGE_TYPE_LABELS) as StorageTypeValue[]

export const WORKFLOW_STATUS_LABELS: Record<WorkflowStatus, string> = {
  DRAFT: 'Draft',
  EDITING: 'Editing',
  METADATA_PENDING: 'Metadata Pending',
  READY: 'Ready for Upload',
  UPLOADING: 'Uploading',
  COMPLETED: 'Completed',
  ARCHIVED: 'Archived',
}

type ChipColor = 'default' | 'primary' | 'success' | 'warning' | 'error' | 'info'

export const WORKFLOW_STATUS_COLORS: Record<WorkflowStatus, ChipColor> = {
  DRAFT: 'default',
  EDITING: 'info',
  METADATA_PENDING: 'warning',
  READY: 'primary',
  UPLOADING: 'info',
  COMPLETED: 'success',
  ARCHIVED: 'default',
}

export const SUBMISSION_STATUS_LABELS: Record<SubmissionStatus, string> = {
  NOT_SUBMITTED: 'Not Submitted',
  SUBMITTED: 'Submitted',
  IN_REVIEW: 'In Review',
  ACCEPTED: 'Accepted',
  REJECTED: 'Rejected',
  RESUBMIT_REQUIRED: 'Resubmit Required',
  RESUBMITTED: 'Resubmitted',
  REMOVED: 'Removed',
}

export const SUBMISSION_STATUS_COLORS: Record<SubmissionStatus, ChipColor> = {
  NOT_SUBMITTED: 'default',
  SUBMITTED: 'info',
  IN_REVIEW: 'warning',
  ACCEPTED: 'success',
  REJECTED: 'error',
  RESUBMIT_REQUIRED: 'warning',
  RESUBMITTED: 'info',
  REMOVED: 'default',
}

export const SUBMISSION_STATUS_OPTIONS: SubmissionStatus[] = [
  'NOT_SUBMITTED', 'SUBMITTED', 'IN_REVIEW', 'ACCEPTED', 'REJECTED',
  'RESUBMIT_REQUIRED', 'RESUBMITTED', 'REMOVED',
]
