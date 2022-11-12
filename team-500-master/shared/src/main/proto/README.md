# SENG302 2022 - LENS gRPC Contracts

This repository is the living 'source of truth' for all gRPC (protobuf) contracts used in the SENG302 2022 project. As fixes and additional features are added to the gRPC contracts, all changes will be updated here.

## History of important (and often breaking) changes  

Sometimes changes will be made to the contracts defined here that will affect, or even break, existing functionality. A history of such changes can be found below:

* _25/7/2022_ - Options for configuring pagination requests have been extracted to re-usable message types to reduce duplication across requests.
  * Pagination options can now be imported as-needed from `util/pagination.proto`

## Frequently asked questions

Below are some of the commonly asked questions we've received, expect this list to grow over time.

### Can I add to, or change the .proto files myself?

No. The SENG302 2022 project "LENSFolio" is part of the LENS ecosystem: a collection of multiple different modules (such as the ScrumBoard) for use in courses at UC.

### I think something's wrong or missing from the contracts, what do I do?

We can almost guarantee that the `.proto` files won't always be correct first time - in fact, from the commit history of this repository it's possible to see a number of fixes added already. Therefore, if you think you've found an issue in the contracts, and you're confident that it is actually an issue (please at least check with your team first!), get in contact with the SENG302 teaching team ASAP. Please include a sufficiently detailed explanation of what the issue is, and how it should be fixed (if you have a solution).

### How should I get in contact with the teaching team to discuss gRPC contracts?

Send an email to Matthew (matthew.minish@pg.canterbury.ac.nz), and CC the shared mailbox (seng302@canterbury.ac.nz).
