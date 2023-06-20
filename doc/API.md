# API

## `POST /patients` fetch patients information by filter

Use `post-as-get` for query

### Query parameters

| Parameter | Type | Description |
|------------|-----|-------------|
| filters | **Patient-info-search**? | Filters parameters for searching.<br> By default, fetching all records|
| sorting | **Sorting**? | Sorting parameters of result records.<br> By default, sorting by `id asc` |
| paging | **Paging** | Parameters of result paging<br>By default, `page-size=100` and  `page-number=1`|

### Response

| Parameter | Type | Description |
|------------|-----|-------------|
| data  | **Patient-info**[] | Result according `filters`, `sorting`, `paging` |
| total | int | Total count of records by `filters` |

### Model

### Patient-info

| Field     | Type   | Description                   |
|-----------|--------|-------------------------------|
| id | long | ID of patient |
|first-name | string? | Patient's first name|
|middle-name| string? | Patient's middle name|
|last-name  | string? | Patient's last name|
| address | string? | Address of patient|
| oms | string? | CMI of patient|
| birth-date| date? | Patient's birth date|
| sex | **Sex**? | Patient's sex|
| deleted | boolean | Flag of deleting record |
| created-at | date-time | Time of creating record | 


### Patient-info-search

Searching strategy for string fields - `starts-with`

| Field     | Type   | Description                   |
|-----------|--------|-------------------------------|
|first-name | string? | Searching value for `first-name` field|
|middle-name| string? | Searching value for `middle-name` field|
|last-name  | string? | Searching value for `last-name` field|
| address | string? | Searching value for `last-name` field|
| oms | string? | Searching value for `oms` field|
| birth-date-period| **Date-period**? | Searching value for `birth-date` field|
| sex-opts | set\[**Sex** & `"undefined"`]? | Searching value for `sex` field. <br> `undefined` means value is absent|
| show-records-opts| **Show-options**?| Strategy of showing records |

### **Date-period**

Format of date-like string `YYYY-MM-DD`

| Field     | Type   | Description      |
|-----------|--------|------------------|
|from       | string? | Start of period |
|to         | string? | End of period   |

### **Sex**

Enumeration

| Value  |
|--------|
| male   |
| female |

### **Show-options**

Enumeration

|Value| Description  |
|-----|--------------|
| all | Show all records include deleted |
| deleted-only | Show only deleted records |
| not-deleted-only | Show only NOT deleted records |


### Request example:

`POST /patients`

body:
```json
{"filters" : {"first-name" : "First",
             "middle-name" : "Middle",
             "last-name" : "Last",
             "address" : "City, street, house",
             "sex" : "male",
             "birth-date": "2020-01-01",
             "oms" : "00000"},
 "sorting" : {"id" "asc"},
 "paging" : {"page-size": 10,
             "page-number: : 1}
```

### Response example

``` json
{"data" : [
            {"first-name" : "First",
             "middle-name" : "Middle",
             "last-name" : "Last",
             "address" : "City, street, house",
             "sex" : "male",
             "birth-date": "2020-01-01",
             "oms" : "0000000000000000"},
             {"first-name" : "First",
             "middle-name" : "Middle",
             "last-name" : "Last",
             "address" : "City, street, house",
             "sex" : "male",
             "birth-date": "2020-01-01",
             "oms" : "0000000000000001"},
             {"first-name" : "First",
             "middle-name" : "Middle",
             "last-name" : "Last",
             "address" : "City, street, house",
             "sex" : "male",
             "birth-date": "2020-01-01",
             "oms" : "0000000000000002"},
             ...
            ]
 "total" : 20}
```


## `GET /patient/:id` get information about patient

`:id` - ID of patient

### Request example

`GET /patient/42`

### Response example

```json
{"id" : 42
 "first-name" : "First",
 "middle-name" : "Middle",
 "last-name" : "Last",
 "address" : "City, street, house",
 "sex" : "male",
 "birth-date": "2019-12-31T21:00:00Z",
 "oms" : "0000000000000000",
 "deleted" : false,
 "updated-at": "2023-05-30T12:35:05Z",
 "created-at": "2023-05-30T12:35:05Z"}
```

## `POST /patient` add information about patient

### Body parameters

| Field     | Type   | Description                   |
|-----------|--------|-------------------------------|
|first-name | string? | Patient's first name|
|middle-name| string? | Patient's middle name|
|last-name  | string? | Patient's last name|
| address | string? | Address of patient|
| oms | string? | CMI of patient|
| birth-date| string? | Patient's birth date <br> Format `YYYY-MM-DD`|
| sex | **Sex**? | Patient's sex|

### Request example

`POST /patient`

body:
```json
{"first-name" : "First",
 "middle-name" : "Middle",
 "last-name" : "Last",
 "address" : "City, street, house",
 "sex" : "male",
 "birth-date": "2020-01-01",
 "oms" : "0000000000000000"}
```

### Response example

```json
{"id" : 42
 "first-name" : "First",
 "middle-name" : "Middle",
 "last-name" : "Last",
 "address" : "City, street, house",
 "sex" : "male",
 "birth-date": "2019-12-31T21:00:00Z",
 "oms" : "0000000000000000",
 "deleted" : false,
 "updated-at": "2023-05-30T12:35:05Z",
 "created-at": "2023-05-30T12:35:05Z"}
```


## `PUT /patient/:id` update information about a patient

`:id` - ID of patient. 

### Body parameters

| Field     | Type   | Description                   |
|-----------|--------|-------------------------------|
| id | long | ID of patient |
|first-name | string? | Patient's first name|
|middle-name| string? | Patient's middle name|
|last-name  | string? | Patient's last name|
| address | string? | Address of patient|
| oms | string? | CMI of patient|
| birth-date| string? | Patient's birth date <br> Format `YYYY-MM-DD`|
| sex | **Sex**? | Patient's sex|

If some defined field is absent - it will be removed.

### Request example

`PUT /patient/42`

body:
```json
{"id" : 42,
 "first-name" : "First",
 "middle-name" : "Middle",
 "last-name" : "Last",
 "address" : "City, street, house",
 "sex" : "male",
 "birth-date": "2020-01-01"}
```

### Response example

```json
{"id" : 42
 "first-name" : "First",
 "middle-name" : "Middle",
 "last-name" : "Last",
 "address" : "City, street, house",
 "sex" : "male",
 "deleted" : false,
 "birth-date": "2019-12-31T21:00:00Z",
 "updated-at": "2023-05-30T12:35:05Z",
 "created-at": "2023-05-30T12:35:05Z"}
```

## `DELETE /patient/:id` mark information about patient as deleted

`:id` - ID of patient

### Request example

`DELETE /patient/42`

### Response example

```json
{"id" : 42,
 "first-name" : "First",
 "middle-name" : "Middle",
 "last-name" : "Last",
 "address" : "City, street, house",
 "sex" : "male",
 "birth-date": "2019-12-31T21:00:00Z",
 "oms" : "0000000000000000",
 "deleted" : true,
 "updated-at": "2023-05-30T12:35:05Z",
 "created-at": "2023-05-30T12:35:05Z"}
 ```

## `POST /patient/:id/restore` undo `DELETE` for information about patient

`:id` - ID of patient

### Request example

`POST /patient/42/restore`

### Response example

```json
{"id" : 42,
 "first-name" : "First",
 "middle-name" : "Middle",
 "last-name" : "Last",
 "address" : "City, street, house",
 "sex" : "male",
 "birth-date": "2019-12-31T21:00:00Z",
 "oms" : "0000000000000000",
 "deleted" : false,
 "updated-at": "2023-05-30T12:35:05Z",
 "created-at": "2023-05-30T12:35:05Z"}
 ```
