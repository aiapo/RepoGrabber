# RepoGrabber
## Description
Grabs GitHub repositories based on specifications, particularly those not in GitHub search
queries, and clones them for further research.

For example: 

- **QUERY:** Get all Java primary , 5000+ KB, repos with 50+ stars, have 300+ commits,
created since 2010 that have 20+ users mentionable on them
- **RETURNS:** 3277 distinct repos to be exported, analysis, cloned, etc
- **TIME:** ~45 minutes

*Please note that this example is based on one previous run and is not indicative of what 
might be in the future/based on different queries.*

## Current:
* CLI find to repos from GitHub with parameters:
  * Amount of followers
  * Language
  * Created from a date to now
  * Percentage of bytes language repo comprises
  * Committer count
  * Total commits over lifetime
  * Total bytes
* Clones repos
* Lists repos
  * Repository ID 
  * Name 
  * Owner 
  * Git URL 
  * Description 
  * Primary language 
  * Creation date 
  * Last update date 
  * Last push date 
  * Is archived/when archived 
  * Is fork/is empty/is locked/is disabled/is template
  * Users assignable
  * Users mentionable
  * Users who have committed
  * Repository size in KB
  * Total commits
  * Number of issues 
  * Number of forks
  * Number of stars
  * Number of watchers
  * Top three languages and their sizes in KB
  * Default branch name
* Integration with RefMiner to show output all refactorings to Database
  * Multithreaded implementation
* Output to CSV
* Import from CSV, skip new query (that RepoGrab made previously)
* Run using Docker

## Planned:
* Runs
  * You would be able to do analysis on different parameters
  * Import a previous run vs start new run
* GUI and/or Web UI (leaning towards web)
* Maybe improve time to retrieve if possible
* Maybe improve time to analyze refactorings
* Cleaner code
* and more!

## Keep in mind
* You need to specify your own GitHub API and PosgreSQL server in config/.env, follow sample
* Every GraphQL call can only get 1,000 repos total
  * So we chunk by creation date
    * There is an optimization algorithm that should make it so that each query
    is between 900-1000 repos
  * Because of the amount of retrieved data, the response is only being stable
  at ~35 repos/page
    * So there is pagination of every query chunked into 40 repos/query
* There is a 3 seconds wait every query to prevent secondary rate limits
* The code is a bit messy, hope to clean it up soon

## Docker usage
You can now run RepoGrabber through Docker. The future idea is for there to be a web server
to do everything but currently there's just a .env file in /config that you can fill out
to run in docker.

TODO: PostgreSQL auto config/start too

You need to fill out this .env file because it will take your variables and run RepoGrabber, then
exit.

To build docker image:
```
docker build -t repograbber . 
```

To run CLI:
(add -d if you want to run in the background)
```
docker run --name repograbber -v "/yourconfigdir:/config" -v "/yourreposdir:/repos" -v 
"/yourresultsdir:/results" repograbber
```