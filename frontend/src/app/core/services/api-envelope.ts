import { ApiResponse } from '../models/api.models';

export function unwrap<T>(response: ApiResponse<T>): T {
  if (!response.status) {
    throw new Error(response.message || 'The request could not be completed.');
  }
  return response.data;
}