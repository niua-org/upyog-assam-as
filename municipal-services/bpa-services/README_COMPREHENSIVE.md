# BPA Service - Complete Guide

## Overview
The Building Plan Approval (BPA) Service handles two main types of applications:
1. **Normal BPA** - For new construction building plan approvals
2. **Occupancy Certificate (OC)** - For completed buildings to get occupancy permission

## 🏗️ What is BPA Service?

BPA Service is like a **digital building permit office** that:
- Processes building plan approval applications
- Issues occupancy certificates for completed buildings
- Manages the entire approval workflow from application to certificate issuance
- Handles fee calculations and payments
- Integrates with various government departments for NOCs

## 🔄 Two Main Flows

### 1. Normal BPA Flow (New Construction)
**Purpose**: Get approval to start construction of a new building

```
Citizen/Architect → DCR Scrutiny → BPA Application → Approval → Construction Permit
```

### 2. Occupancy Certificate Flow (Completed Building)
**Purpose**: Get permission to occupy/use a completed building

```
Building Owner → OC Application → Inspection → Compliance Check → Occupancy Certificate
```

---

## 📋 Normal BPA Flow - Step by Step

### Phase 1: Pre-Application (DCR Scrutiny)
```
1. Architect prepares building plans
2. Submits plans to DCR (Digital Construction Rules) system
3. DCR system validates plans against building rules
4. Gets DCR scrutiny number (if approved)
```

### Phase 2: BPA Application Creation
```
POST /bpa-services/v1/bpa/_create

Required Data:
- DCR scrutiny number
- Land details (from land-services)
- Applicant information
- Building details (floors, area, usage type)
- Required documents
```

**Key Documents Required:**
- Ownership proof
- Site plan
- Building plan drawings
- NOC from various departments
- Structural stability certificate

### Phase 3: Application Processing
```
Application States Flow:
INITIATED → DOCUMENT_VERIFY → FIELD_INSPECTION → APPROVAL → PERMIT_ORDER
```

**Workflow Steps:**
1. **Document Verification** - Officials verify submitted documents
2. **Field Inspection** - Site inspection by municipal officials
3. **NOC Processing** - Automatic integration with NOC services
4. **Fee Calculation** - BPA calculator service computes fees
5. **Payment** - Integration with billing and collection services
6. **Final Approval** - Permit order generation

### Phase 4: Approval & Permit
```
On Approval:
- Approval number generated
- Permit order PDF created
- SMS notifications sent
- Construction can begin
```

---

## 🏠 Occupancy Certificate Flow - Step by Step

### Phase 1: OC Application Creation
```
POST /bpa-services/v1/bpa/_create
(with applicationType = "OCCUPANCY_CERTIFICATE")

Required Data:
- Original BPA approval number
- Completion certificate
- Building completion details
- Compliance documents
```

### Phase 2: Compliance Verification
```
Application States Flow:
OC_INITIATED → OC_DOCUMENT_VERIFY → OC_SITE_INSPECTION → OC_APPROVAL → OC_ISSUED
```

**Verification Steps:**
1. **Document Check** - Verify completion certificates
2. **Site Inspection** - Physical verification of completed building
3. **Compliance Check** - Ensure building matches approved plans
4. **Safety Verification** - Fire safety, structural safety checks
5. **Utility Connections** - Water, electricity, sewage connections verified

### Phase 3: OC Issuance
```
On Approval:
- Occupancy Certificate number generated
- OC certificate PDF created
- Building can be legally occupied
```

---

## 🗄️ Database Structure

### Core Tables

#### `ug_bpa_buildingplans`
Main application table storing:
- Application details (application_no, status, dates)
- Business service type (BPA vs OC)
- Land and EDCR references
- Approval information

#### `ug_bpa_documents`
Document management:
- Document types and file references
- Links to filestore service
- Document verification status

#### `ug_bpa_rtp_detail`
RTP (Right to Practice) professional details:
- Architect/Engineer information
- Professional license verification
- Assignment tracking

#### `ug_bpa_area_mapping_detail`
Geographic and authority mapping:
- District, planning area details
- Authority assignments (Municipal/Panchayat)
- Administrative boundaries

---

## 🔌 API Endpoints

### Core Operations
```bash
# Create new application (BPA or OC)
POST /bpa-services/v1/bpa/_create

# Update existing application
PUT /bpa-services/v1/bpa/_update

# Search applications
POST /bpa-services/v1/bpa/_search

# Get application details
GET /bpa-services/v1/bpa/_search?applicationNumber={appNo}
```

### Sample Request - Normal BPA
```json
{
  "RequestInfo": {...},
  "BPA": {
    "tenantId": "pb.amritsar",
    "businessService": "BPA",
    "applicationType": "NEW_CONSTRUCTION",
    "edcrNumber": "EDC-2024-001",
    "landId": "LAND-123",
    "documents": [...],
    "additionalDetails": {...}
  }
}
```

### Sample Request - Occupancy Certificate
```json
{
  "RequestInfo": {...},
  "BPA": {
    "tenantId": "pb.amritsar", 
    "businessService": "BPA_OC",
    "applicationType": "OCCUPANCY_CERTIFICATE",
    "edcrNumber": "OCDCR-2024-001",
    "documents": [...],
    "additionalDetails": {
      "originalBpaNumber": "BPA-2023-001"
    }
  }
}
```

---

## 🔄 Integration Points

### Service Dependencies

#### Essential Services
- **egov-user** - User management and authentication
- **egov-workflow-v2** - Application state management
- **egov-filestore** - Document storage
- **egov-idgen** - Application number generation

#### Business Services  
- **dcr-services** - Plan scrutiny validation
- **land-services** - Property/land information
- **noc-services** - NOC processing
- **bpa-calculator** - Fee calculation

#### Supporting Services
- **billing-service** - Demand and bill generation
- **collection-services** - Payment processing
- **pdf-service** - Certificate generation
- **egov-notification-sms** - SMS alerts

### Kafka Integration
```yaml
Consumers:
- egov.collection.payment-create  # Payment notifications

Producers:
- save-bpa-buildingplan          # New applications
- update-bpa-buildingplan        # Application updates  
- update-bpa-workflow            # Workflow state changes
```

---

## 🎯 Key Features

### Workflow Management
- **Configurable States** - Different workflows for BPA vs OC
- **Role-based Actions** - Different actions for different user roles
- **Auto-transitions** - Automatic state changes based on conditions

### Fee Management
- **Dynamic Calculation** - Fees calculated based on building parameters
- **Multiple Components** - Application fee, scrutiny fee, development charges
- **Payment Integration** - Seamless payment processing

### Document Management
- **Type Validation** - Ensures required documents are uploaded
- **Version Control** - Tracks document updates
- **Digital Signatures** - Support for digitally signed documents

### Notification System
- **SMS Alerts** - Status updates via SMS
- **Email Notifications** - Detailed updates via email
- **In-app Notifications** - Real-time updates in citizen portal

---

## 🚀 Quick Start

### For Normal BPA Application
1. Get DCR scrutiny approval
2. Prepare required documents
3. Create BPA application via API
4. Track application status
5. Make payments when demanded
6. Receive approval and permit order

### For Occupancy Certificate
1. Complete building construction
2. Get completion certificate
3. Create OC application via API
4. Schedule site inspection
5. Address any compliance issues
6. Receive occupancy certificate

---

## 📊 Application States

### Normal BPA States
```
INITIATED → DOCUMENT_VERIFY → FIELD_INSPECTION → 
APPROVAL → PERMIT_ORDER → REVOCATED (if needed)
```

### Occupancy Certificate States  
```
OC_INITIATED → OC_DOCUMENT_VERIFY → OC_SITE_INSPECTION → 
OC_APPROVAL → OC_ISSUED → OC_REVOCATED (if needed)
```

---

## 🔍 Search & Filter Options

Applications can be searched by:
- Application Number
- Applicant Name  
- Mobile Number
- Status
- Date Range
- Business Service Type (BPA/OC)
- Tenant ID

---

## 📱 User Roles & Permissions

### Citizen/Architect
- Create applications
- Upload documents
- Track status
- Make payments

### Municipal Officials
- Verify documents
- Conduct inspections
- Approve/reject applications
- Generate certificates

### System Admin
- Configure workflows
- Manage master data
- Generate reports

---

This README provides a complete understanding of how the BPA service works for both normal building plan approvals and occupancy certificates, making it easy for developers and stakeholders to understand the system's functionality.