import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Subject, catchError, debounceTime, of, switchMap, tap } from 'rxjs';
import { CartItem, OrderResponse, Product, SortOption, StoreInfo } from './product.model';
import { ShopService } from './shop.service';

const CART_KEY = 'aranthangi-mart.cart';
const CUSTOMER_KEY = 'aranthangi-mart.customer';
const PHONE_PATTERN = /^[6-9]\d{9}$/;

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  private readonly shop = inject(ShopService);
  private readonly refresh$ = new Subject<void>();

  store: StoreInfo = {
    name: 'Aranthangi Mart',
    tagline: 'Fresh groceries from Aranthangi, delivered to your door',
    deliveryFee: 30,
    freeDeliveryAbove: 499
  };

  products: Product[] = [];
  categories: string[] = ['All'];
  category = 'All';
  searchText = '';
  sort: SortOption = 'default';
  loading = false;
  error = '';

  cart: CartItem[] = [];
  cartOpen = false;
  customerName = '';
  phone = '';
  address = '';
  placingOrder = false;
  orderError = '';
  lastOrder: OrderResponse | null = null;

  ordersOpen = false;
  ordersPhone = '';
  ordersLoading = false;
  ordersError = '';
  myOrders: OrderResponse[] | null = null;

  constructor() {
    this.refresh$.pipe(
      debounceTime(250),
      tap(() => { this.loading = true; this.error = ''; }),
      switchMap(() => this.shop.getProducts(this.searchText, this.category, this.sort).pipe(
        catchError(() => {
          this.error = 'Could not load products. Is the backend running on port 8080?';
          return of(null);
        })
      )),
      takeUntilDestroyed()
    ).subscribe(items => {
      if (items) this.products = items;
      this.loading = false;
    });
  }

  ngOnInit(): void {
    this.restoreState();
    this.shop.getStoreInfo().subscribe({ next: info => (this.store = info), error: () => undefined });
    this.shop.getCategories().subscribe({
      next: cats => (this.categories = ['All', ...cats]),
      error: () => undefined
    });
    this.loadProducts();
  }

  // ---- catalog ----
  loadProducts(): void { this.refresh$.next(); }

  selectCategory(cat: string): void {
    this.category = cat;
    this.loadProducts();
  }

  clearSearch(): void {
    this.searchText = '';
    this.loadProducts();
  }

  stockLabel(product: Product): string {
    if (product.stock === 0) return 'Out of stock';
    if (product.stock <= 5) return `Only ${product.stock} left`;
    return '';
  }

  scrollToProducts(): void {
    document.getElementById('products')?.scrollIntoView({ behavior: 'smooth' });
  }

  // ---- cart ----
  cartIndex(product: Product): number {
    return this.cart.findIndex(item => item.product.id === product.id);
  }

  qtyInCart(product: Product): number {
    return this.cart.find(item => item.product.id === product.id)?.quantity ?? 0;
  }

  addToCart(product: Product): void {
    const found = this.cart.find(item => item.product.id === product.id);
    if (found) {
      if (found.quantity >= product.stock) return;
      found.quantity += 1;
    } else if (product.stock > 0) {
      this.cart.push({ product, quantity: 1 });
    }
    this.orderError = '';
    this.persistCart();
  }

  changeQty(item: CartItem, delta: number): void {
    const next = item.quantity + delta;
    if (next <= 0) { this.remove(item); return; }
    if (next > item.product.stock) return;
    item.quantity = next;
    this.persistCart();
  }

  remove(item: CartItem): void {
    this.cart = this.cart.filter(current => current !== item);
    this.persistCart();
  }

  get cartCount(): number {
    return this.cart.reduce((sum, item) => sum + item.quantity, 0);
  }

  get cartSubtotal(): number {
    return this.cart.reduce((sum, item) => sum + item.product.price * item.quantity, 0);
  }

  get deliveryFee(): number {
    if (this.cart.length === 0 || this.cartSubtotal >= this.store.freeDeliveryAbove) return 0;
    return this.store.deliveryFee;
  }

  get cartTotal(): number { return this.cartSubtotal + this.deliveryFee; }

  get amountForFreeDelivery(): number {
    return Math.max(0, this.store.freeDeliveryAbove - this.cartSubtotal);
  }

  get phoneValid(): boolean { return PHONE_PATTERN.test(this.phone.trim()); }

  get canCheckout(): boolean {
    return !this.placingOrder && this.cart.length > 0 && this.phoneValid
      && this.customerName.trim().length > 0 && this.address.trim().length > 0;
  }

  // ---- checkout ----
  checkout(): void {
    if (!this.canCheckout) return;
    this.placingOrder = true;
    this.orderError = '';
    this.shop.placeOrder({
      customerName: this.customerName.trim(),
      phone: this.phone.trim(),
      address: this.address.trim(),
      items: this.cart.map(item => ({ productId: item.product.id, quantity: item.quantity }))
    }).subscribe({
      next: order => {
        this.lastOrder = order;
        this.cart = [];
        this.persistCart();
        this.saveCustomer();
        this.placingOrder = false;
        this.loadProducts(); // stock has changed
      },
      error: (err: HttpErrorResponse) => {
        this.orderError = err.error?.message ?? 'Order failed. Please try again.';
        this.placingOrder = false;
        this.loadProducts(); // refresh stock so the shopper sees what is still available
      }
    });
  }

  // ---- my orders ----
  openOrders(): void {
    this.ordersOpen = true;
    if (!this.ordersPhone && this.phoneValid) this.ordersPhone = this.phone.trim();
  }

  loadOrders(): void {
    const phone = this.ordersPhone.trim();
    if (!PHONE_PATTERN.test(phone)) {
      this.ordersError = 'Enter the 10-digit mobile number used for your order.';
      return;
    }
    this.ordersLoading = true;
    this.ordersError = '';
    this.shop.getOrders(phone).subscribe({
      next: orders => { this.myOrders = orders; this.ordersLoading = false; },
      error: () => { this.ordersError = 'Could not load orders. Please try again.'; this.ordersLoading = false; }
    });
  }

  statusLabel(status: string): string {
    return status.replaceAll('_', ' ').toLowerCase();
  }

  // ---- local persistence (best effort) ----
  private persistCart(): void {
    try { localStorage.setItem(CART_KEY, JSON.stringify(this.cart)); } catch { /* storage unavailable */ }
  }

  private saveCustomer(): void {
    try {
      localStorage.setItem(CUSTOMER_KEY, JSON.stringify({
        name: this.customerName.trim(), phone: this.phone.trim(), address: this.address.trim()
      }));
    } catch { /* storage unavailable */ }
  }

  private restoreState(): void {
    try {
      const cart = JSON.parse(localStorage.getItem(CART_KEY) ?? '[]');
      if (Array.isArray(cart)) {
        this.cart = cart.filter((item: CartItem) => item?.product?.id && item.quantity > 0);
      }
      const customer = JSON.parse(localStorage.getItem(CUSTOMER_KEY) ?? 'null');
      if (customer) {
        this.customerName = customer.name ?? '';
        this.phone = customer.phone ?? '';
        this.address = customer.address ?? '';
      }
    } catch { /* ignore corrupted storage */ }
  }
}
