Group 8
Pedro Mendonça 73896, Ivo Pinto 73888, Tiago Pereira 76352

Functionality tests:

The functionality tests are divided in 3 test classes in junit.

testFSINIT: 
	tests the correctness of the FS_init function

testFSREAD: 
	test the correctness of the FS_read function, by performing 3 tests:
		reading from a valid position with a size that does not surpasses EOF
		reading from a valid position with a size that surpasses EOF
		reading from a position that surpasses the EOF

testWRITE: 
	test the correctness of the FS_write function, by performing 12 tests. These tests aim to ensure that all corner cases are correct.
	The first 6 tests try writing on an empty file, with combinations of:
		starting in the first position or not
		writing content that would fit in one content block or that would need more than one
	The last 6 writing over an existing file, testing particulars such as:
		overwriting data
		writing after EOF
		writing over various content blocks

///////////////////////////////////////////////

Integrity tests:

To test the integrity of the project the server has a set of functions that alter the contents of either the Header Blocks or the Content Block.

For the Header Blocks we test that the client detects when either the Contents Blocks which the header is composed of, the signature or the public key were tampered.
For the Content Blocks we test that when the content of a Content Block is altered the client detects it.

These tests are in the class testIntegrity.