export interface Product {
  id: number;
  name: string;
  description: string;
  category: string;
  unit: string;
  price: number;
  stock: number;
  emoji: string;
}

export interface CartItem {
  product: Product;
  quantity: number;
}

export interface StoreInfo {
  name: string;
  tagline: string;
  deliveryFee: number;
  freeDeliveryAbove: number;
}

export interface OrderRequest {
  customerName: string;
  phone: string;
  address: string;
  items: Array<{ productId: number; quantity: number }>;
}

export interface OrderResponse {
  id: number;
  customerName: string;
  phone: string;
  address: string;
  status: string;
  subtotal: number;
  deliveryFee: number;
  total: number;
  createdAt: string;
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
  }>;
}

export type SortOption = 'default' | 'priceAsc' | 'priceDesc' | 'name';
