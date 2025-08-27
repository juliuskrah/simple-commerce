import type { User } from '$lib/types';
import type { ProductVariantDetail } from './$houdini';

export interface PageData {
	user: User;
	ProductVariantDetail: typeof ProductVariantDetail;
}
