A collection of util scripts.

## Instructions

You need to create a file named 'password' in the 'data' directory that contains only the password for the 'gpc' user. The script will read the password from this file. Obviously, do not commit this file (there is an ignore on it)

## Utils

### Create

#### Description

Creates a new GPC repo (but does not configure commit access).

#### Args

    [«name»] [«description»]

If the name and/or description are omitted, they will be prompted for.

### Delete

#### Description

Deletes a GPC repo.

#### Args

    [«name»]

If the name is omitted, it will be prompted for.

### Grant

#### Description

Grants commit access to repos to all members listed in the members file.

#### Args

    [«repo»] [«repo»] …

Run without args, all repositories will used. Otherwise, each of the arguments given will be used.