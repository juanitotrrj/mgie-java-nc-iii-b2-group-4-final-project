# User Guide

Instructions for operators and end users of the G4 Inventory Management System (desktop client + API server).

## Who Uses What

| Role | Typical tasks |
|------|----------------|
| **Administrator** | Users, settings, backups, audit logs, full inventory and sales |
| **Manager** | Approvals, purchases, sales oversight, reports, dashboards |
| **Inventory Clerk** | Products, categories, suppliers, purchases, change requests |
| **Cashier** | Sales (POS), receipts, product lookup |

Permissions are assigned per role during setup. The client shows menu items only when your account has the matching permission.

---

## Part 1: Server (Operators)

Operators start and maintain the API service. End users normally only run the desktop client.

### Start the server

```bash
cd inventory-server
java -jar inventory-server-1.0.0.jar
```

Leave the window open or run as a Windows service (see [DEPLOYMENT.md](DEPLOYMENT.md)).

### Check that the server is healthy

- Browser: `http://localhost:8080/api/health`
- Command: `java -jar inventory-server-1.0.0.jar health`

### Browse the API (Swagger)

Open `http://localhost:8080/api/docs` to try endpoints (useful for support, not required for daily staff).

### When the system is “not ready”

If the database shows setup state other than `INITIALIZED`, business APIs return **503**. Complete the **client setup wizard** (Part 2 below) before staff log in.

---

## Part 2: Client — First Launch

### Launch the application

```bash
java -jar inventory-client-1.0.0.jar
```

Or use a desktop shortcut pointing to the same command.

### A. Initial setup wizard (one time)

Shown when the server is not yet `INITIALIZED`.

| Step | What you do |
|------|-------------|
| 1. Status | Confirm server connection and setup state |
| 2. Root auth | Log in with **root** credentials from server CLI setup |
| 3. Business settings | Company name, address, tax, currency, etc. |
| 4. Roles | Confirm or seed system roles |
| 5. Permissions | Map permissions to roles |
| 6. Users | Create administrator and staff accounts |
| 7. Review & finish | Submit; system becomes `INITIALIZED` |

After finish, close and reopen the client, or continue to the guest screen.

### B. Guest mode (every day before login)

Navigation: **Welcome** | **About** | **Contact** | **Test Connection** | **Login**

- **Welcome** — Overview of the system  
- **About** — Module list and architecture summary  
- **Contact** — Submit a public inquiry (stored via API)  
- **Test Connection** — Verifies `GET /api/health`  
- **Login** — Opens login form on the same window  

### C. Login

1. Enter **username** and **password**  
2. Click **Login**  
3. On success, the main application opens  

If login fails, check caps lock, account status (Active), and that the server is running.

### D. Main application

- **Header:** your name, role, **Logout**  
- **Sidebar:** modules you are allowed to use  
- **Content:** selected module screen  

**Logout** returns to the guest screen (session cleared).

---

## Part 3: Modules by Feature

### Dashboard

Role-specific home screen with metric cards and summary tables.

| Role | Dashboard highlights |
|------|---------------------|
| Administrator | System-wide metrics, recent audit activity |
| Manager | Pending change requests, low stock, sales summary |
| Inventory Clerk | Your requests, inventory activity |
| Cashier | Today’s sales, recent transactions |

Select **Dashboard** in the sidebar (default after login).

### Products

- **List:** search, status filter, pagination  
- **Add / Edit:** name, SKU, category, price, reorder level, status  
- **Delete:** deactivates product (server rules apply)  
- **Refresh:** reload list  
- **Export:** download CSV (save dialog)  

Stock quantity changes for clerks should go through **Change Requests**, not direct edits, when your policy requires approval.

### Categories

- List, add, edit, deactivate categories  
- Search and export similar to products  

### Suppliers

- Maintain supplier contact details and status  
- Search, CRUD, export  

### Purchases

| Action | Description |
|--------|-------------|
| **Add** | Create purchase order with line items (product, qty, unit cost) |
| **Receive** | Mark order received (updates stock) |
| **Cancel** | Cancel with reason |
| **Filter** | Status: Pending, Delivered, Received, Cancelled |

### Sales (POS)

| Action | Description |
|--------|-------------|
| **New Sale** | Customer name, payment method, line items, amount received, change |
| **View Receipt** | View details for selected sale |
| **Cancel** | Cancel sale (may restore stock per server rules) |

### Change Requests (ICR)

**Clerk:** submit adjustment/correction/damage/lost/found with quantity and reason.

**Manager / Admin:** approve or reject pending requests.

Use filters for Pending / Approved / Rejected.

### Stock Movements

Read-only history of stock in/out/adjustments with filters (product, movement type, dates).

### Reports

1. Choose report type (Sales Summary, Inventory Value, Purchase Cost, Low Stock, Top Selling)  
2. Enter date range (YYYY-MM-DD) and optional category  
3. Click **Generate** — preview in text area  
4. **Export** — save CSV via file dialog  

### Users (Administrator)

- List users with role and status filters  
- Add / edit users  
- Deactivate accounts  
- Reset password  
- Export user list  

### Settings (Administrator)

Tabbed configuration loaded from the server database:

| Tab | Contents |
|-----|----------|
| Company | Name, address, contact |
| Inventory | Thresholds, costing rules |
| Security | Session and password policies |
| Notifications | Email toggles (if SMTP configured) |
| Database | Test connection, backup |

Click **Save** per tab after changes.

### Audit Logs (Administrator)

Search by user, action, module; paginated read-only log of system activity.

---

## Part 4: Roles and Access (Reference)

After standard migration seed, roles roughly map as follows:

| Capability | Administrator | Manager | Clerk | Cashier |
|------------|:-------------:|:-------:|:-----:|:-------:|
| Dashboard | ✓ | ✓ | ✓ | ✓ |
| Products (read/write) | ✓ | ✓ | ✓ | read |
| Categories / Suppliers | ✓ | ✓ | ✓ | — |
| Purchases | ✓ | ✓ | ✓ | — |
| Sales | ✓ | ✓ | — | ✓ |
| Reports / Export | ✓ | ✓ | — | — |
| Change requests (submit) | ✓ | — | ✓ | — |
| Change requests (approve) | ✓ | ✓ | — | — |
| Users / Settings / Audit | ✓ | — | — | — |

Exact menu items depend on permissions returned at login. If a menu is missing, contact an administrator.

---

## Part 5: Tips and Policies

- **Session timeout:** configured on server (`G4IMS_SERVER_AUTH_TOKEN_EXPIRY_SECONDS`); re-login when actions fail with unauthorized errors.  
- **Password rules:** minimum length and complexity from server `.env`.  
- **Backups:** Administrators should run backups before upgrades or bulk imports (Settings → Database).  
- **Support:** use **Contact** on guest screen or check server logs in `inventory-server/logs/`.  

## Technical Reference

- Client architecture: [client/TECHNICAL.md](client/TECHNICAL.md)  
- API details: [server/API.md](server/API.md)  
- Run/troubleshoot: [RUN.md](RUN.md)  
