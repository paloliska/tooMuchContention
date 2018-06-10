# tooMuchContention
test tooMuchContentionException throwed by Debry

This code is trying to make derby throw tooMuchContentionException during generating new id for inserting row.
There is one test class with 2 tests. One is built to fail and one is built to pass.
Only difference in tests is method getCountFailing() (called TransactioTemplate) vs. getCountPassing() (called service method anotated with @Transactional)
