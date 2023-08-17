# RepoGrabber
## Description
Grabs repositories based on specifications and clones them for further research.

For example: 
- Clone all Java based repos have 300 commits since 2010 that have at least ten users assignable on them.

## Current:
* CLI find to repos based on:
  * Amount of followers
  * Language
  * Push activity
  * Percentage of bytes language repo comprises
  * Assignable users
  * Total commits over lifetime
  * Total bytes
* Grabs straight from GitHub's GraphQL API
* Clones repos
* Lists repos
* Output to CSV

## Planned:
* Maybe a GUI
* Import CSV to skip querying GitHub
* Cleaner code
* and more!

## Keep in mind
* You need to specify your own GitHub API in keys.config, follow sample
* Every GraphQL call can only get 1,000 repos total even with pagination, hence why the queries are chunked every 4 months
  * This should allow you to get all repos, BUT it will take longer
* There is a 10 seconds wait every query to prevent secondary rate limits
* The code is a bit messy, I hope to clean it up soon