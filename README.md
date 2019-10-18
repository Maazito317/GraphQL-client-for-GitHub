# GitHub Navigator

GraphQL client for GitHub.

## Author
Maaz Khan <667 601 660>

## Summary

User is prompted for the username of a Github user that the user wants more information about.
The user is returned a menu to be able to run a query on either a single repository or all the repositories of the provided Github user.

For a single repository, you can view:
* Repository details (name, description, url, createdAt, updatedAt)
* Languages of this repository (first 5 according to default config)
* Issues of this repository (first 10 according to default config)

For all the repositories (first 100 according to default config), we can view:
* Their names
* The name of the repository with the
    * Most issues
    * Most forks
    * Most stargazers

The user is presented with an option to return to main menu to launch another query.

## Usage

The program can be run directly from IntelliJ or simply using `sbt` while in the source directory (followed by the `run`) on the `sbt` console. There are no command line arguments.

The program control variables can be controlled using the `application.conf` file.
```
http-config {  
    BASE_GHQL_URL="https://api.github.com/graphql"  
    Authorization = "<YOUR_TOKEN>"  
    Accept_Type = "application/json"  
}  
  
query-vars {  
    repo-count = "100"  
    issue-count = "10"  
    language-count = "5"  
}
```
The `http-config` are the configurations for HTTP Client Builder. The `BASE_GHQL_URL` represents the location of the GitHub GraphQL API which needs to be given the GitHub Developer provided authentication token `Authorization`.

The `query-vars` represents the variables that are used in the individual queries made to the GraphQL API. `repo-count` represents the number of repositories on which to perform the all repositories query (Option 2 in command line interface). `issue-count` represents the number of issues that you would like to be returned for a particular repository query and the `language-count` represents the number of top languages that you would like to be printed out for a particular repository.

## Design Patterns Used
### Creational Design Patterns
For creational design patterns, I have used Abstract Factory pattern in which the `Querent` class is the main factory for all of the underlying classes used during the execution of the program. This allowed us to have a unified base for the `RepoQuerent`, `UserQuerent`, `ReposQuerent` classes that are used for searching Github. 

### Structural Design Patterns
For structural design patterns, I have implemented two design patterns. Namely, Composite and Adaptor. 

Each of the aforementioned querents provide a search function that is called by the main loop of the program to search Github for the desired query. This allowed the main loop to remain coherent, simple and easy to read.  The `RepoQuerent`, `UserQuerent` and `ReposQuerent` all inherit functions from a common parent class that contains the client as protected so that the client only needs to be stored in a single place instead of the individual classes. This further allowed for a more streamlined structure in which the responsibility of each `Class` was well-defined. The `*Querent` classes have a simple semantic that allows the main loop to perform search queries, the `Querent` handles the networking side of the query. Once response has been received, it is the responsibility of the `*Querent` classes to parse the response and return a user-friendly representation of the results.

The data extracted from the HTTPResponse by GraphQL is designed in a composite fashion. The parent `Data` class that all responses are extracted in is templatized to handle all query structures and the types `UserAllDetails`, `Repo` and `AllRepoDetails` are the data types that the data from `UserQuerent`, `ReposQuerent` and `RepoQuerent` is extracted into. The `edges` and `node` is a repetitive pattern in all `Connection` objects so we use templatized `Edges` and `Node` to handle `RepositoriesConnections`, `LanguageConnections` and `IssueConnections`.

### Behavioral Design Patterns
For behavioral design patterns, we made sure to use the Iterator pattern. This allowed us to have safe, clean and concise code wherever we used any kind of looping on well-defined data.

For details of my understanding of design patterns and a comprehensive list of the design patterns that were available to me please refer to this [blog post](https://sourcemaking.com/design_patterns).

## Benchmark
### CPU
The CPU utilization of my implementation is fairly low with an average of ~7-10% throughout the execution of the program with 25 queries. There are instantaneous spikes and relatively longer dips in the utilization. 

The spikes are attributed to the time that the CPU spends between performing the network interaction of queries. For each query a new connection is initialized (because there is a [resource limitation](https://developer.github.com/v4/guides/resource-limitations/) on a single client instance) and when the CPU executes a lot of sequential instructions to parse the results of the query. 

The dips are due to the relatively longer periods of inactivity between when a request has been sent over the network and the we must wait for a response from the Github server.

### Memory
The memory utlilization does not exceed ~15MB after having normalized for the JVM under the default `gc` threshold and memory parameters for the JVM. This memory accounts for the internal data structures used for the execution of the program.
