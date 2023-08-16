# RepoGrabber
## Description
Grabs repositories based on query and clones them for further research.

Example: 
- Clone all Java based repos that were committed to in the last ten years that have at least five users active on the project.

## Current:
* CLI to repos based on:
  * Language(s)
  * Publish dates
  * Push activity
  * Private status
  * Archive status
* Clones repos
* Lists repos
* Output to CSV

## Planned:
* Maybe a GUI
* Use *Specified Language Bytes/Total Bytes* to determine if a repo should be cloned
* Use *Community Users* to determine if a repo should be cloned
* Use *Total Bytes* to determine if a repo should be cloned
* Pagination of repos (priority as it currently only gets up to 100!)
* Cleaner code
* and more!