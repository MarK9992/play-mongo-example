# Play - MongoDB example

The goal of this example is to create a REST API with the Play framework to perform CRUD operations over persons stored
in MongoDB. The Reactive Mongo driver is used in order to achieve that in a total asynchronous manner and data moves
along the network in JSON format. Also, JSON data is validated and/or transformed in order to match some requirements.

## Setup

### MongoDB

Be sure you have a mongo daemon (v3.2 preferred) running at `localhost:27017` with no authentication.

### Build

At the project root, run `sbt`, then in the sbt shell `run`.
REST APIs are now available at `localhost:9000`
You can also execute automated tests in the sbt shell with `test` command.

## Features

### Persons REST API

 * create a person: `POST /person Content-Type: application/json` with a request body set to a JSON person
 * list persons: `GET /persons`
 * update a person: `PUT /person/:id Content-Type: application/json` with a request body set to a JSON person
 * remove a person: `DELETE /persons/:id`
 * list persons in a different format: `GET /v2/persons`
 
#### Person JSON format

```
{
    "name": "foo",
    "lastName": "bar",
    "birthDate": "yyyy-mm-dd",
    "sex": "female",
    "addresses": {
        "personal": {
            "street": "blah",
            "town": "foobar",
            "zipCode": "777"
        }
        ...
    }
}
```
 * names have to be at least 3 characters length
 * age has to be at least 18 and maximum 99
 * sex can only be either "male" or "female"
 * addresses are optional
 * addresses can only be either of type "personal" or "professional"
 * street, town and zipCode have to be at least 1 character length

### Addresses REST API

 * add an address to a person: `POST /person/:id/address/:addressType Content-Type: application/json` with a request body set to a JSON address
 * update a person's address: `PUT /person/:id/address/:addressType Content-Type: application/json` with a request body set to a JSON address
 * remove a person's address: `DELETE /person/:id/address/:addressType`
 
#### Address JSON format

```
{
    "street": "blah",
    "town": "foobar",
    "zipCode": "777"
}
```
 * street, town and zipCode have to be at least 1 character length

### Persistence

Persons and addresses are stored in MongoDB through ReactiveMongo driver. Person ids can be retrieved only via direct database look-up.
 
### Tests

Behavior tests of both persons API and addresses API are automated.

## Demonstration scenario

### Step 1: Create a person

Send a `POST` request to `localhost:9000/person` with HTTP header `Content-Type: application/json` and the following request body:

```
{
    "name": "foo",
    "lastName": "bar",
    "birthDate": "1990-01-01",
    "sex": "female"
}
```
It may throw an error `Could not request database` the first time, that means the database connection hasn't been
initialized yet, just retry.

### Step 2: Display the person you just created

Send a `GET` request to `localhost:9000/persons`.

### Step 3: Modify the name of your person

First, run a find query in your mongo-shell to retrieve the ObjectID of the person you want to modify. Then, send a
`PUT` request to `localhost:9000/person/:id` with `:id` set to the value you just retrieved and with HTTP header
`Content-Type: application/json` and the following request body:

```
{
    "name": "buzz",
    "lastName": "bar",
    "birthDate": "1990-01-01",
    "sex": "female"
}
```
You can re-execute step 2 in order to acknowledge that your modifications have been taken into account.

### Step 4: Add an address to that person

Send a `POST` request to `localhost:9000/person/:id/address/personal` with `:id` set to the ObjectID you retrieved from MongoDB
and with HTTP header `Content-Type: application/json` and the following request body:
```
{
    "street": "foo",
    "town": "foobar",
    "zipCode": "777"
}
```

### Step 5: Display your person in another format

Send a `GET` request to `localhost:9000/v2/persons`.

### Step 6: Update one of that person's addresses

Send a `PUT` request to `localhost:9000/person/:id/address/personal` with `:id` set to the ObjectID you retrieved from MongoDB
and with HTTP header `Content-Type: application/json` and the following request body:
```
{
    "street": "foo",
    "town": "foobar",
    "zipCode": "666"
}
```

### Step 7: Remove one of that person's addresses

Send a `DELETE` request to `localhost:9000/person/:id/address/personal` with `:id` set to the ObjectID you retrieved from MongoDB.

### Step 8: Delete a person

Send a `DELETE` request to `localhost:9000/person/:id` with `:id` set to the ObjectID you retrieved from MongoDB.

## Links

 * [Play framework](https://www.playframework.com/)
 * [Reactive Mongo](http://reactivemongo.org/)
 * [MongoDB](https://www.mongodb.com/)
 * [JSON format](http://www.json.org/)