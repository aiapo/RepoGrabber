# RepoGrabber
## Description
Grabs GitHub repositories based on specifications, particularly those not in GitHub search
queries, and clones them for further research.

For example: 

- **QUERY:** Get all Java primary , >=5000 bytes, repos with 50 stars, have 300 commits,
created since 2010 that have at least 20 users mentionable on them
- **RETURNS:** 3277 distinct repos to be exported, analysis, cloned, etc
- **TIME:** ~45 minutes

*Please note that this example is based on one previous run and is not indicative of what 
might be in the future/based on different queries.*

## Current:
* CLI find to repos based on:
  * Amount of followers
  * Language
  * Created from a date to now
  * Percentage of bytes language repo comprises
  * Mentionable users
  * Total commits over lifetime
  * Total bytes
* Grabs straight from GitHub's GraphQL API
* Clones repos
* Lists repos
* Output to CSV

## Planned:
* Maybe a GUI
* Import CSV to skip querying GitHub
* Integration with RefMiner
* Maybe improve time to retrieve if possible
* Cleaner code
* and more!

## Keep in mind
* You need to specify your own GitHub API in keys.config, follow sample
* Every GraphQL call can only get 1,000 repos total
  * So we chunk by creation date
    * There is an optimization algorithm that should make it so that each query
    is between 900-1000 repos
  * Because of the amount of retrieved data, the response is only being stable
  at ~40 repos/page
    * So there is pagination of every query chunked into 40 repos/query
* There is a 3 seconds wait every query to prevent secondary rate limits
* The code is a bit messy, hope to clean it up soon