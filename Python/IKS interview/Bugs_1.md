One issue I worked on was a false `REVIEW_REQUIRED` problem in our onboarding platform. Some applications with valid documents were unnecessarily being sent for manual review, which was increasing processing time and operational effort.

I was asked to investigate because the issue involved the boundary between Document Processing and KYC, which I had worked on.

I traced a few affected applications using correlation IDs and followed the data from Document Service → Document Processing → KYC. I found that for one document type, the DOB was being extracted in a different format from what our downstream system expected.

Our validation was too permissive, so the value wasn't rejected at the extraction boundary. It passed into KYC, where the DOB cross-check failed and triggered `REVIEW_REQUIRED`.

So the KYC logic wasn't actually the problem. The root cause was an upstream data-contract and validation issue.

I fixed it at the Document Processing boundary by making the DOB validation stricter, normalizing the supported formats, and adding a confidence threshold for extracted fields. I also added PyTest regression tests for the affected document types and formats.

After the fix, the false `REVIEW_REQUIRED` cases dropped to near zero. We also used the finding to improve validation practices across our other document-processing flows.

The main takeaway was: **when a downstream decision looks wrong, trace the data across the boundaries instead of just patching the downstream logic.**
