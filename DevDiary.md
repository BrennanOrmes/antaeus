## Development Diary
Development Diary - What is the purpose?
This will outline my general thought process and day to day activity of the development process for this challenge.

### Development

#### Tuesday 5th of April (1 hour)
I had set up a linux VM on virtual box to run and develop the code in an isolated environment.

Looked into the code to see how the structure works, and see if there is any similarities to what I have done before
with .NET Core

From what I can see so far, it is pretty similar to how I have set up API projects in Core. Which is useful since I
sort of know my way around now and can follow the code pretty easily.

Initial observations:
There is no use of automapper, or similar libraries, thus making the mapping of objects a little
tedious, but that is OK since doing it manually means less room for error.

I had some issues trying to run the docker file following the guide in the README, but I saw there was a bash,
so I ran that, and the API is running now.

My initial thoughts on how to tackle this challenge was thinking about how I would do it in C#. Typically, I would
make use of an external library like hangfire to set routine jobs to fire and forget. Hangfire has its own dashboard
built in, which is very useful if you are building APIs with tools like swagger for API documentation.
The dashboard allows you to manually fire scheduled jobs and also queue them to be rescheduled if they have failed.

#### Wednesday 6th of April (1 hour)
From analyzing the challenge and code, I made the following assumptions:

1. Depending on where the service is located, it will go by that services timezone to fire at midnight

2. The limit for retrying payments would be 3.

3. We set the status of the invoice to failed if the customers currency does not match the currency of the invoice. This is feasible since there is an exception for it.

I started to look into potential libraries to use that offer similar functionality to hangfire, and one potential
candidate that I have found is JobRunr. JobRunr has its own dashboard that lets you see what jobs are running and which 
ones have failed, this might prove useful since right now I am not sure if I will build a front end to show a list of 
invoices.

I implemented a service method that takes a string then filters the results of the fetchAll by getting the Enum
value of InvoiceStatus.

#### Friday 8th of April (2 hours)
Spent some time reading about how to interact with entities from the database. I then used what I had learned to
implement a updateInvoiceStatus method. I chose to write a method that only updates the invoices' status, since
further improvements to the system would allow a customer to add adjustments to an invoice.

I could assume that invoices might be made in error, but from my limited knowledge of these systems, I
will assume that the invoice can just be marked as paid or a different status, and then a new invoice can be
created, with the correct values

With this method, I can update the status, and only the status, without accidentally making changes to other columns
in the database. I'd be curious to know if you can make a catch-all update method, that you can then provide the
body of the update query from the service layer into the dal. Something to note for further improvements.

I also added unit tests for the new service methods I have introduced. However, I had some difficulty for the
behaviour verification of my fetch by status method, since it would not let me return an explicit list of invoices.
Will need to do some further reading on the MockK library.

#### Saturday 9th of April (2 hours)
Wrote unit tests for the billing service and then expanded the service to use the payment provider. I originally
developed the service to update the invoice with a different status depending on if the provider had succeeded or if 
any exceptions were thrown, but my first implementation had many calls to the invoice service to update, and in my 
opinion it did not read very well. I refactored the code to only make a call to the invoice service at the end, and pass
in a status enum where it's value was determined by the payment provider service / exceptions.

When I finished up the service for charging invoices, I realised my unit tests were incorrect. I was mocking the service
itself that I was testing, not the services it was using. Thankfully I only had to rewrite the services being mocked, 
but it took some time as I had to read up on the mockk documentation to get a better grasp on how the library works.

#### Sunday 10th of April (2 hours)
Wrote batch method that can be called by a cron job. The batch method is exposed over HTTP so external job scheduling 
services can call it (Kubernetes or AWS Lambda).
The batch handles the retrying of pending jobs returned from the charge invoices method. Unit tests for the cron job was 
interesting, as I was not sure how to test a void method. Reading into the mockk documentation, I learned that you can 
verify how many times a method was called, and that was perfect for my scenario as I wanted to see if the retry would 
call the update method in the invoice service with a pending status.

#### Monday 11th of April (1 hour)
Spent some time implementing JobRunr. Things seemed to be going smoothly until I hit an error that JobRunr was 
incompatible with the current version of kotlin. This is a shame, since it has support for recurring jobs scheduling.

### Improvements

- Move the batch method to its own service, this would allow it to be easier to test, since you can mock the services it will use
- Move the mocked methods that define specific behaviour into their respective tests
- Implement JWT bearer authentication, as the REST right now does not have any authentication
- Implement a failed job notification service that can notify customers of failed payments
- Implement a job log service that writes logs from the batch method
- Implement logging that can interface with azure application insights
- Implement a generic service base class for CRUD operations to the database. (Not sure how feasible this is in kotlin)

### Summary

Overall, I have spent roughly 8 hours on this challenge. Most of the time spent was on learning kotlin and reading the 
documentation. I personally really enjoyed learning about the mockk framework, as I currently have not had too much
exposure to testing libraries. I really look forward to getting feedback on this challenge, especially on how I can
improve the unit tests code coverage.