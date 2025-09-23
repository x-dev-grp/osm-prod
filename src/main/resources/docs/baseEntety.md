# Project Documentation

## Overview

This project provides a framework for managing multiple types of entities that share common properties and behavior
through a base class. The system allows for operations such as creating, updating, deleting, and retrieving different
types of entities (e.g., `WasteType`, `SupplierType`, `OliveLotStatusType`, `OliveVarietyType`) that extend a shared
base class called `BaseType`.

The main components of the project include:

- **BaseType**: A common abstract class for all entity types.
- **Repository Layer**: Interfaces for database interaction with each specific entity type.
- **Service Layer**: Contains business logic for handling operations on entities.
- **Exception Handling**: Centralized exception handling for uniform error responses.

---

## 1. **Base Class (`BaseType`)**

The `BaseType` class acts as a parent class for all entity types, encapsulating shared fields like `id`, `createdAt`,
and `updatedAt`. This class is abstract and not directly mapped to the database.

### Code Example: `BaseType`

```java
package com.osm.oilproductionservice.model.customTypes;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseType {
    @Id
    private Long id;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Getter and Setter methods for id, createdAt, and updatedAt
}
```

### Key Points:

- **`@MappedSuperclass`**: This annotation makes `BaseType` a non-entity superclass, but its fields are inherited by
  subclasses.
- **`@CreationTimestamp`** and **`@UpdateTimestamp`**: These annotations automatically populate `createdAt` and
  `updatedAt` fields when an entity is created or updated.

---

## 2. **Entity Classes**

Each entity type (e.g., `WasteType`, `SupplierType`, `OliveLotStatusType`) extends `BaseType` and adds its own unique
fields.

### Example: `WasteType`

```java
package com.osm.oilproductionservice.model.customTypes;

import com.xdev.xdevbase.entities.BaseEntity;

import javax.persistence.Entity;

@Entity
public class WasteType extends BaseEntity {
    private String name;
    private String description;

    // Getter and Setter methods for name and description
}
```

### Example: `SupplierType`

```java
package com.osm.oilproductionservice.model.customTypes;

import com.xdev.xdevbase.entities.BaseEntity;

import javax.persistence.Entity;

@Entity
public class SupplierType extends BaseEntity {
    private String supplierName;
    private String supplierAddress;

    // Getter and Setter methods for supplierName and supplierAddress
}
```

### Explanation:

- Each specific entity class inherits from `BaseType` and can define additional attributes (e.g., `name`, `description`,
  `supplierName`).

---

## 3. **Repository Layer**

Each entity type has a corresponding repository that interacts with the database. These repositories extend
`JpaRepository`, providing standard CRUD operations and custom query methods.

### Example: `WasteTypeRepository`

```java
package com.osm.oilproductionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WasteTypeRepository extends JpaRepository<WasteType, Long> {
    boolean existsByName(String name);
}
```

### Explanation:

- **`existsByName`**: A custom query method to check if a `WasteType` entity already exists with the given name.

Similarly, repositories for `SupplierType`, `OliveLotStatusType`, and `OliveVarietyType` also provide methods for
interacting with the database, such as checking for existing records.

---

## 4. **Service Layer**

The service layer contains the business logic for handling CRUD operations on the entities. It uses repositories to
interact with the database and provides methods to manage the entities.

### Example: `GenericTypeService`

```java
package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.model.customTypes.*;
import com.osm.oilproductionservice.exception.ServiceException;
import org.springframework.stereotype.Service;

@Service
public class GenericTypeService {

  private final WasteTypeRepository wasteTypeRepository;
  private final SupplierTypeEntityRepository supplierTypeEntityRepository;
  private final OliveVarietyTypeRepository oliveVarietyTypeRepository;
  private final OliveLotStatusTypeRepository oliveLotStatusTypeRepository;

  public GenericTypeService(WasteTypeRepository wasteTypeRepository, SupplierTypeEntityRepository supplierTypeEntityRepository, OliveVarietyTypeRepository oliveVarietyTypeRepository, OliveLotStatusTypeRepository oliveLotStatusTypeRepository) {
    this.wasteTypeRepository = wasteTypeRepository;
    this.supplierTypeEntityRepository = supplierTypeEntityRepository;
    this.oliveVarietyTypeRepository = oliveVarietyTypeRepository;
    this.oliveLotStatusTypeRepository = oliveLotStatusTypeRepository;
  }

  // Create a new type (e.g., WasteType, SupplierTypeEntity, OliveLotStatusType)
  public Object createType(BaseType baseType, String type) {
    switch (type.toLowerCase()) {
      case "wastetype":
        if (wasteTypeRepository.existsByName(((WasteType) baseType).getName())) {
          throw new ServiceException("A WasteType with this name already exists.");
        }
        return wasteTypeRepository.save((WasteType) baseType);

      case "suppliertype":
        if (supplierTypeEntityRepository.existsByName(((SupplierType) baseType).getName())) {
          throw new ServiceException("A Supplier with this name already exists.");
        }
        return supplierTypeEntityRepository.save((SupplierType) baseType);

      case "olivelotstatustype":
        if (oliveLotStatusTypeRepository.existsByName(((OliveLotStatusType) baseType).getName())) {
          throw new ServiceException("An OliveLotStatusType with this name already exists.");
        }
        return oliveLotStatusTypeRepository.save((OliveLotStatusType) baseType);

      case "olivevarietytype":
        if (oliveVarietyTypeRepository.existsByName(((OliveVarietyType) baseType).getName())) {
          throw new ServiceException("An OliveVarietyType with this name already exists.");
        }
        return oliveVarietyTypeRepository.save((OliveVarietyType) baseType);

      default:
        throw new ServiceException("Unknown type: " + type);
    }
  }

  // Additional methods (getAllTypes, getType, updateType, deleteType) follow a similar pattern...
}
```

### Explanation:

- The `createType` method checks if an entity already exists by its name before saving it. If the entity exists, a
  `ServiceException` is thrown.
- The service layer interacts with the repository layer to perform CRUD operations on each type of entity.

---

## 5. **Exception Handling**

A centralized exception handling mechanism is implemented using a `@ControllerAdvice` class to capture and handle any
errors thrown in the service layer.

### Example: Global Exception Handler

```java
package com.osm.oilproductionservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

---

## 6. **Steps to Add a New Entity**

To add a new entity to the project, follow these steps:

### 1. **Create the Entity Class**

- Create a new class that extends `BaseType`. Add the specific fields and business logic that are relevant to your new
  entity.

#### Example: `NewEntity`

```java
package com.osm.oilproductionservice.model.customTypes;

import com.xdev.xdevbase.entities.BaseEntity;

import javax.persistence.Entity;

@Entity
public class NewEntity extends BaseEntity {
    private String name;
    private String description;

    // Getter and Setter methods for name and description
}
```

### 2. **Create the Repository Interface**

- Create a new repository interface that extends `JpaRepository`. Add any custom query methods needed (e.g.,
  `existsByName`).

#### Example: `NewEntityRepository`

```java
package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.customTypes.NewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewEntityRepository extends JpaRepository<NewEntity, Long> {
    boolean existsByName(String name);
}
```

### 3. **Update the Service Layer**

- Add logic to the `GenericTypeService` class to handle operations for the new entity. This includes checks for existing
  entities and saving new instances.

#### Example Update to `GenericTypeService`

```java
package com.osm.oilproductionservice.service;

public Object createType(BaseType baseType, String type) {
    switch (type.toLowerCase()) {
        case "newentity":
            if (newEntityRepository.existsByName(((NewEntity) baseType).getName())) {
                throw new ServiceException("A NewEntity with this name already exists.");
            }
            return newEntityRepository.save((NewEntity) baseType);
        // Other cases remain unchanged
    }
}
```

### 4. **Testing**

- Once the entity, repository, and service layer are updated, ensure you write appropriate test cases to validate the
  functionality of your new entity.

---

## Conclusion

This project utilizes a common `BaseType` class for shared fields across multiple entity types, allowing for streamlined
management of various entities with different attributes. The repository and service layers work together to ensure that
CRUD operations are performed efficiently, and centralized exception handling ensures a consistent response for any
errors encountered.

---
