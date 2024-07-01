
## How to Create the Runtime Type in API Control Plane?

It is essential to create the *runtime type* in API Control Plane. By default, the runtime types - *webMethods API Gateway* and *webMethods Developer Portal* are already created within API Control Plane. For details, see [Runtime Type Management REST API](https://github.com/SoftwareAG/webmethods-api-control-plane/blob/main/apis/openapi-specifications/runtime-type.yaml). 

Use the following REST API to create the *runtime type* in API Control Plane:

``` POST /api/assetcatalog/v1/runtimes/types ```

For example:

```

POST https://localhost:8080/api/assetcatalog/v1/runtimes/types
Content-Type: application/json
{
"id":"Azure", // ID of the runtime type
"name":"azure_runtime" // Name of the runtime type
}

```

Skip this procedure if your runtime type is already created in API Control Plane.
