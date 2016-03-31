Group 8
Pedro Mendonça 73896, Ivo Pinto 73888, Tiago Pereira 76352

This README covers the new tests implemented in the second part of the project only.

Functionality tests:

The functionality tests are divided in 5 test classes in junit.

testsFSINIT: 
	tests the correctness of the FS_init function (now checks that the certificate present in the CC is stored correctly)

testsFSLIST:
	tests the correctness of the FS_list() function. it consist of 2 tests:
		check that list works with one CC registered in the server
		check that list works with more than one CC registered in the server (since we only have one functonal CC we use dummy certificates)

testsFSWRITE: 
	test the correctness of the FS_write function. it requires the use of a correct CC with its correct PIN code

testsFSREAD: 
	test the correctness of the FS_read function (it now identifies which file to read using its public key)

testsCertificates:
	tests that only certificates which are validated by the CAs of Cartao de Cidadao are usable

///////////////////////////////////////////////

Integrity tests:

To test the aditional integrity provided by our solution we use the class testsIntegrity which performs the following test:
	perform the init function with one CC, switch the CC with another one and attempt to write something to the file, which is not possible.