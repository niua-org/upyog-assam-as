# UPYOG Web - Frontend Implementation Guide

## Overview
This directory contains the web implementation of UPYOG (Urban Platform for deliverY of Online Governance) UI modules. This guide focuses on the **OBPSV2 (Online Building Plan Approval System V2)** module architecture and implementation patterns.

## Table of Contents
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [OBPSV2 Module Architecture](#obpsv2-module-architecture)
- [Multistep Form Implementation](#multistep-form-implementation)
- [Hooks System](#hooks-system)
- [Services Layer](#services-layer)
- [API Endpoints (URLs)](#api-endpoints-urls)
- [Development Guidelines](#development-guidelines)

---

## Project Structure

```
web/
├── micro-ui-internals/
│   ├── packages/
│   │   ├── libraries/              # Core libraries and utilities
│   │   │   └── src/
│   │   │       ├── hooks/          # Custom React hooks
│   │   │       │   └── obpsv2/     # OBPSV2-specific hooks
│   │   │       └── services/       # API service layer
│   │   │           ├── atoms/      # Service utilities
│   │   │           │   └── urls.js # API endpoint definitions
│   │   │           └── elements/   # Service implementations
│   │   │               └── OBPSV2.js
│   │   ├── modules/                # Feature modules
│   │   │   └── obpsv2/            # OBPSV2 module
│   │   │       └── src/
│   │   │           ├── components/ # Reusable components
│   │   │           ├── config/     # Form configurations
│   │   │           ├── pageComponents/ # Form step components
│   │   │           ├── pages/      # Route pages
│   │   │           │   ├── citizen/
│   │   │           │   └── employee/
│   │   │           ├── utils/      # Utility functions
│   │   │           └── Module.js   # Module registration
│   │   └── react-components/       # Shared UI components
│   └── example/                    # Example implementation
├── src/                            # Application entry point
│   ├── App.js
│   ├── ComponentRegistry.js
│   └── Customisations/
├── public/                         # Static assets
└── package.json                    # Dependencies
```

---

## Getting Started

### Prerequisites
- Node.js >= 14
- Yarn package manager

### Installation & Setup

1. **Navigate to web directory:**
   ```bash
   cd web
   ```

2. **Install dependencies:**
   ```bash
   yarn install
   ```

3. **Start development server:**
   ```bash
   yarn start
   ```
   The application will run on `http://localhost:3000`

4. **Build for production:**
   ```bash
   yarn build
   ```

### Updating Modules
To pull latest updates from micro-ui-internals:
```bash
./install-deps.sh
```

---

## OBPSV2 Module Architecture

### Module Overview
OBPSV2 (Online Building Plan Approval System V2) is a comprehensive module for managing building permit applications in Assam. It handles the complete lifecycle from application creation to approval.

### Key Features
- **Area Mapping**: District, planning area, and authority mapping
- **Property Validation**: Validates property details against existing records
- **Applicant Management**: Personal and address details collection
- **Land Details**: Plot information and adjoining land owners
- **Document Management**: Upload and manage required documents
- **RTP (Registered Technical Person)**: Technical person registration and management
- **Form Generation**: Auto-generation of Form 22, Form 23A, and Form 23B
- **GIS Integration**: Geographic information system integration
- **Workflow Management**: Multi-stage approval workflow

### Module Registration

The module is registered in `Module.js`:

```javascript
const componentsToRegister = {
  OBPSV2Module,
  OBPSV2Links,
  ApplicantDetails,
  AddressDetails,
  DocumentDetails,
  Form22A,
  Form23A,
  Form23B,
  LandDetails,
  PropertyValidation,
  BPACreate: Create,
  RTPCreate,
  RTPInbox: Inbox,
  OBPSV2Inbox,
  OBPSV2Card: OBPSV2EmployeeCard,
  CheckPage,
  BPAMyApplications,
  AreaMapping,
  RTPForm,
  SiteReport,
  BPAAcknowledgement,
  RTPAcknowledgement,
  BPAEdit: Edit,
  OBPASCitizenHomeScreen,
  RTASearchApplication,
  BPAApplicationDetails,
  BPAEmployeeDetails
}

export const initOBPSV2Components = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};
```

---

## Multistep Form Implementation

### Configuration-Driven Forms

OBPSV2 uses a configuration-driven approach for multistep forms. The configuration is defined in `config/buildingPermitConfig.js`:

```javascript
export const buildingPermitConfig = [
  {
    head: "ES_TITILE_APPLICANT_DETAILS",
    body: [
      {
        route: "area-mapping",
        component: "AreaMapping",
        withoutLabel: true,
        key: "areaMapping",
        type: "component",
        nextStep: "property-validation",
        hideInEmployee: true,
        isMandatory: true,
        texts: {
          header: "BPA_AREA_MAPPING",
          submitBarLabel: "CS_COMMON_NEXT",
        },
      },
      {
        route: "property-validation",
        component: "PropertyValidation",
        key: "propertyValidation",
        nextStep: "applicant-details",
        // ... more config
      },
      {
        route: "applicant-details",
        component: "ApplicantDetails",
        key: "applicant",
        nextStep: "address-details",
        // ... more config
      },
      {
        route: "address-details",
        component: "AddressDetails",
        key: "address",
        nextStep: "land-details",
        // ... more config
      },
      {
        route: "land-details",
        component: "LandDetails",
        key: "land",
        nextStep: null, // null means go to check page
        // ... more config
      }
    ],
  },
];
```

### Form Flow Management

The main Create component (`pages/citizen/Create/index.js`) manages the form flow:

```javascript
const Create = () => {
  const [params, setParams, clearParams] = Digit.Hooks.useSessionStorage("OBPSV2_CREATE", {});
  
  // Navigate to next step
  const goNext = (skipStep, index, isAddMultiple, key) => {
    let currentPath = pathname.split("/").pop();
    let { nextStep } = config.find((routeObj) => routeObj.route === currentPath);
    
    if (nextStep === null) {
      return redirectWithHistory(`${match.path}/check`);
    }
    
    let nextPage = `${match.path}/${nextStep}`;
    redirectWithHistory(nextPage);
  };

  // Handle form data selection
  function handleSelect(key, data, skipStep, index, isAddMultiple = false) {
    setParams({ ...params, ...{ [key]: { ...params[key], ...data } } });
    goNext(skipStep, index, isAddMultiple, key);
  }

  return (
    <Switch>
      {config.map((routeObj, index) => {
        const Component = Digit.ComponentRegistryService.getComponent(routeObj.component);
        return (
          <Route path={`${match.path}/${routeObj.route}`} key={index}>
            <Component 
              config={{ texts: routeObj.texts, inputs: routeObj.inputs, key: routeObj.key }} 
              onSelect={handleSelect} 
              formData={params} 
            />
          </Route>
        );
      })}
      
      <Route path={`${match.path}/check`}>
        <CheckPage onSubmit={acknowledgement} value={params} />
      </Route>
      
      <Route path={`${match.path}/acknowledgement`}>
        <BPAAcknowledgement data={params} onSuccess={onSuccess} />
      </Route>
    </Switch>
  );
};
```

### Step Components

Each step is a separate component in `pageComponents/`:

**Example: ApplicantDetails.js**
```javascript
const ApplicantDetails = ({ config, onSelect, formData }) => {
  const [applicantData, setApplicantData] = useState(formData?.applicant || {});
  
  const handleSubmit = () => {
    onSelect(config.key, applicantData);
  };
  
  return (
    <FormComposer
      config={config}
      onSubmit={handleSubmit}
      defaultValues={applicantData}
    />
  );
};
```

### Data Persistence

Form data is persisted using session storage:
- **Key**: `OBPSV2_CREATE`
- **Storage**: Browser session storage
- **Hook**: `Digit.Hooks.useSessionStorage()`

---

## Hooks System

### OBPSV2 Custom Hooks

Located in `libraries/src/hooks/obpsv2/`:

#### 1. **useBPACreateUpdateApi** (Create/Update Operations)

```javascript
// File: useBPACreateUpdateApi.js
import { useMutation } from "react-query";
import { OBPSV2Services } from "../../services/elements/OBPSV2";

export const useBPACreateUpdateApi = (tenantId, flow) => {
  if (flow === "create") {
    return useMutation((data) => OBPSV2Services.create(data, tenantId));
  }
  return useMutation((data) => OBPSV2Services.update(data, tenantId));
};
```

**Usage:**
```javascript
const { mutate, isLoading } = useBPACreateUpdateApi(tenantId, "create");

mutate(applicationData, {
  onSuccess: (response) => {
    console.log("Application created:", response);
  },
  onError: (error) => {
    console.error("Error:", error);
  }
});
```

#### 2. **useBPASearchApi** (Search Operations)

```javascript
// File: useBPASearchApi.js
const useBPASearchApi = ({ tenantId, filters, auth }, config = {}) => {
  const client = useQueryClient();
  
  const { isLoading, error, data, isSuccess, refetch } = useQuery(
    [tenantId, filters, auth, config], 
    () => Digit.OBPSV2Services.search({ tenantId, filters, auth }), 
    {
      select: (data) => {
        if (data.bpa.length > 0) {
          data.bpa[0].applicationNo = data.bpa[0].applicationNo || [];
        }
        return data;
      },
      ...config,
    }
  );

  return { 
    isLoading, 
    error, 
    data, 
    isSuccess, 
    refetch, 
    revalidate: () => client.invalidateQueries([tenantId, filters, auth]) 
  };
};
```

**Usage:**
```javascript
const { data, isLoading, error } = useBPASearchApi({
  tenantId: "assam",
  filters: { applicationNo: "BPA-2024-001" },
  auth: true
});
```

#### 3. **useOBPSV2Search** (Unified Search)

```javascript
// File: useOBPSV2Search.js
const useOBPSV2Search = (selectedType, payload, tenantId, filters, params, config = {}) => {
  return useBPAV2Search(tenantId, filters, config);
};
```

#### 4. **useBPAV2DetailsPage** (Application Details)

Fetches complete application details including documents, forms, and payment information.

#### 5. **useEstimateDetails** (Fee Calculation)

Calculates permit fees and charges.

#### 6. **useScrutinyFormDetails** (Form Generation)

Generates Form 22, Form 23A, and Form 23B from EDCR data.

### Hook Calling Pattern

```javascript
// In a component
const MyComponent = () => {
  // 1. Search for applications
  const { data: applications, isLoading } = useBPASearchApi({
    tenantId: "assam",
    filters: { status: "PENDING" }
  });

  // 2. Create/Update mutation
  const { mutate: createApplication } = useBPACreateUpdateApi("assam", "create");

  // 3. Handle form submission
  const handleSubmit = (formData) => {
    createApplication(formData, {
      onSuccess: (response) => {
        // Navigate to acknowledgement
      }
    });
  };

  return (
    // Component JSX
  );
};
```

---

## Services Layer

### OBPSV2Services Object

Located in `libraries/src/services/elements/OBPSV2.js`:

```javascript
export const OBPSV2Services = {
  // Create new application
  create: (details) =>
    Request({
      url: Urls.obpsv2.create,
      data: details,
      method: "POST",
      auth: true,
    }),

  // Update existing application
  update: (details) =>
    Request({
      url: Urls.obpsv2.update,
      data: details,
      method: "POST",
      auth: true,
    }),

  // Search applications
  search: ({ tenantId, filters, auth }) =>
    Request({
      url: Urls.obpsv2.search,
      method: "POST",
      auth: auth === false ? auth : true,
      params: { tenantId, ...filters },
    }),

  // RTP (Registered Technical Person) create
  rtpcreate: (data, tenantId) =>
    Request({
      url: Urls.edcr.create,
      multipartData: data,
      method: "POST",
      params: { tenantId },
      auth: true,
      multipartFormData: true,
    }),

  // RTP search
  rtpsearch: (details) =>
    Request({
      url: Urls.obpsv2.rtpsearch,
      data: details,
      method: "POST",
      auth: true,
    }),

  // Property validation
  propertyValidate: (data, params) =>
    Request({
      url: Urls.obpsv2.propertyValidate,
      data: data,
      params: { ...params },
      method: "POST",
      auth: true,
    }),

  // NOC validation
  nocValidate: (details) =>
    Request({
      url: Urls.obpsv2.nocValidate,
      data: details,
      method: "POST",
      auth: true,
    }),

  // Fee estimation
  estimate: (data, params) =>
    Request({
      url: Urls.obpsv2.estimate,
      data: data,
      params: { ...params },
      method: "POST",
      auth: true,
    }),

  // GIS service integration
  gisService: (data) =>
    Request({
      url: Urls.obpsv2.gisService,
      multipartData: data,
      method: "POST",
      auth: true,
      multipartFormData: true,
    }),

  // GIS zone search
  gisSearch: (data) => {
    const isFormData = data instanceof FormData;
    return Request({
      url: Urls.obpsv2.gisSearch,
      ...(isFormData ? { multipartData: data } : { data: data }),
      method: "POST",
      auth: true,
      ...(isFormData && { multipartFormData: true }),
    });
  },

  // NOC search
  NOCSearch: (tenantId, sourceRefId) =>
    Request({
      url: Urls.obpsv2.nocSearch,
      params: { tenantId, ...sourceRefId },
      method: "POST",
      auth: true,
    }),

  // Application details with forms
  BPAApplicationDetails: async (tenantId, filters) => {
    // Fetches complete application details including:
    // - Basic application info
    // - Area mapping
    // - Applicant details
    // - Land details
    // - Documents
    // - Form 22, 23A, 23B (auto-generated from EDCR)
    // - Payment details
    // - Property validation details
  },
};
```

### ScrutinyFormService

Handles EDCR (Electronic Development Control Regulations) scrutiny details:

```javascript
export const ScrutinyFormService = {
  async getDetails(edcrNumber, tenantId) {
    const response = await Digit.OBPSService.scrutinyDetails(tenantId, { edcrNumber });
    const scrutinyData = response?.edcrDetail?.[0];
    
    // Extracts and formats:
    // - Form 22: Plot area, floor calculations, coverage, FAR
    // - Form 23A: Bylaws, setbacks, parking, fees
    // - Form 23B: Purpose, sanitary details, construction validity
    
    return { form22, form23A, form23B };
  },
};
```

---

## API Endpoints (URLs)

### URL Configuration

Located in `libraries/src/services/atoms/urls.js`:

```javascript
const Urls = {
  obpsv2: {
    // Core CRUD operations
    create: "/bpa-services/v1/bpa/_create",
    update: "/bpa-services/v1/bpa/_update",
    search: "/bpa-services/v1/bpa/_search",
    
    // RTP (Registered Technical Person)
    rtpcreate: "/bpa-services/v1/rtp/_create",
    rtpsearch: "/bpa-services/v1/bpa/_rtpsearch",
    
    // Validation services
    propertyValidate: "/bpa-services/v1/property/validate",
    nocValidate: "/noc-services/v1/noc/_validate",
    
    // Fee calculation
    estimate: "/bpa-services/v2/bpa/_estimate",
    
    // GIS integration
    gisService: "/gis-service/find-zone",
    gisSearch: "/gis-service/zone/_search",
    
    // NOC services
    updateNOC: "/noc-services/v1/noc/_update",
    nocSearch: "/noc-services/v1/noc/_search",
  },
  
  edcr: {
    // EDCR scrutiny
    create: "/edcr/rest/dcr/scrutinize",
    anonymousCreate: "/edcr/rest/dcr/anonymousScrutinize"
  },
  
  // Common services
  FileStore: "/filestore/v1/files",
  FileFetch: "/filestore/v1/files/url",
  
  payment: {
    fetch_bill: "/billing-service/bill/v2/_fetchbill",
    create_reciept: "/collection-services/payments/_create",
    reciept_search: "/collection-services/payments/:buisnessService/_search",
  },
  
  // ... other module URLs
};
```

### API Call Flow

```
Component
    ↓
Custom Hook (useBPACreateUpdateApi)
    ↓
Service Layer (OBPSV2Services.create)
    ↓
Request Utility (with URL from urls.js)
    ↓
Backend API
```

### Example: Complete Create Flow

```javascript
// 1. Component initiates create
const CreateApplication = () => {
  const { mutate } = useBPACreateUpdateApi("assam", "create");
  
  const handleSubmit = (formData) => {
    const payload = {
      bpa: {
        tenantId: "assam",
        landInfo: formData.land,
        applicant: formData.applicant,
        // ... more data
      }
    };
    
    mutate(payload);
  };
};

// 2. Hook calls service
useBPACreateUpdateApi → OBPSV2Services.create(data)

// 3. Service makes API call
Request({
  url: "/bpa-services/v1/bpa/_create",  // From Urls.obpsv2.create
  data: payload,
  method: "POST"
})

// 4. Backend processes and returns response
```

---

## Development Guidelines

### Adding a New Form Step

1. **Create page component** in `pageComponents/`:
   ```javascript
   // NewStep.js
   const NewStep = ({ config, onSelect, formData }) => {
     const handleSubmit = (data) => {
       onSelect(config.key, data);
     };
     return <FormComposer onSubmit={handleSubmit} />;
   };
   ```

2. **Add to configuration** in `config/buildingPermitConfig.js`:
   ```javascript
   {
     route: "new-step",
     component: "NewStep",
     key: "newStep",
     nextStep: "next-step-route",
     texts: { header: "NEW_STEP_HEADER" }
   }
   ```

3. **Register component** in `Module.js`:
   ```javascript
   const componentsToRegister = {
     // ... existing
     NewStep,
   };
   ```

### Creating a Custom Hook

```javascript
// libraries/src/hooks/obpsv2/useCustomHook.js
import { useQuery } from "react-query";

const useCustomHook = (params) => {
  return useQuery(
    ["custom-key", params],
    () => Digit.OBPSV2Services.customMethod(params),
    {
      enabled: !!params,
      staleTime: 5000,
    }
  );
};

export default useCustomHook;
```

### Adding a New API Endpoint

1. **Add URL** in `urls.js`:
   ```javascript
   obpsv2: {
     // ... existing
     newEndpoint: "/bpa-services/v1/new/_endpoint",
   }
   ```

2. **Add service method** in `OBPSV2.js`:
   ```javascript
   export const OBPSV2Services = {
     // ... existing
     newMethod: (data) =>
       Request({
         url: Urls.obpsv2.newEndpoint,
         data: data,
         method: "POST",
         auth: true,
       }),
   };
   ```

3. **Create hook** (optional):
   ```javascript
   const useNewMethod = () => {
     return useMutation((data) => Digit.OBPSV2Services.newMethod(data));
   };
   ```

### Best Practices

1. **State Management**:
   - Use session storage for form data persistence
   - Use React Query for server state
   - Use local state for UI-only state

2. **Error Handling**:
   ```javascript
   const { mutate } = useBPACreateUpdateApi(tenantId, "create");
   
   mutate(data, {
     onError: (error) => {
       setShowToast({ error: true, message: error.message });
     }
   });
   ```

3. **Loading States**:
   ```javascript
   const { isLoading, data } = useBPASearchApi({ tenantId, filters });
   
   if (isLoading) return <Loader />;
   ```

4. **Data Validation**:
   - Validate on client side before API calls
   - Use form validation libraries
   - Handle backend validation errors

5. **Code Organization**:
   - Keep components small and focused
   - Extract reusable logic into hooks
   - Use configuration for form definitions

---

## Key Concepts

### 1. Configuration-Driven Development
Forms are defined in configuration files, making them easy to modify without changing component code.

### 2. Component Registry
Components are registered globally and can be accessed anywhere using `Digit.ComponentRegistryService.getComponent()`.

### 3. Session Storage Pattern
Form data persists across page refreshes using session storage with a unique key per module.

### 4. React Query Integration
All API calls use React Query for caching, loading states, and error handling.

### 5. Multistep Navigation
Navigation between steps is handled by the parent Create component based on configuration.

---

## Troubleshooting

### Common Issues

1. **Form data not persisting**:
   - Check session storage key: `OBPSV2_CREATE`
   - Ensure `setParams` is called in `handleSelect`

2. **Component not found**:
   - Verify component is registered in `Module.js`
   - Check component name matches configuration

3. **API call failing**:
   - Verify URL in `urls.js`
   - Check authentication token
   - Validate request payload structure

4. **Hook not updating**:
   - Check React Query cache key
   - Use `refetch()` or `invalidateQueries()` to force update

---

## Additional Resources

- [UPYOG Documentation](https://upyog-docs.gitbook.io/upyog-v-1.0/)
- [React Query Docs](https://react-query.tanstack.com/)
- [React Router Docs](https://reactrouter.com/)

---

## License
UPYOG Source Code is open source under [UPYOG CODE, COPYRIGHT AND CONTRIBUTION LICENSE TERMS](https://upyog.niua.org/employee/Upyog%20Code%20and%20Copyright%20License_v1.pdf)
