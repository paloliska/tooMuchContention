# tooMuchContention
test tooMuchContentionException thrown by Derby

This code is trying to make derby throw tooMuchContentionException during generating new id for inserting row.
There is one test class with 3 tests. 
All test *could* fail, but depends on hardware, setup and load.
 
Setup: 
 - TEST_TIMEOUT = how long will each test run at most until fail
 - nThreads = number of threads
 
 Note: mixing @Transactional with TransactionTemplate seems to fail more often.