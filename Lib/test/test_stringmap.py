import unittest
from test import test_support

from test_userdict import TestMappingProtocol
from org.python.core import PyStringMap

class SimpleClass:
    pass

class StringMapTest(TestMappingProtocol):
    _tested_class = None

class ClassDictTests(StringMapTest):
    """Check that class dicts conform to the mapping protocol"""

    def _empty_mapping(self):
        for key in SimpleClass.__dict__.copy():
            SimpleClass.__dict__.pop(key)
        return SimpleClass.__dict__

class InstanceDictTests(StringMapTest):
    def _empty_mapping(self):
        return SimpleClass().__dict__

class PyStringMapTest(StringMapTest):
    _tested_class = PyStringMap

    def test_all(self):
        d = PyStringMap()
        # Test __setitem__
        d["one"] = 1

        # Test __getitem__
        self.assertEqual(d["one"], 1)
        self.assertRaises(KeyError, d.__getitem__, "two")

        # Test __delitem__
        del d["one"]
        self.assertRaises(KeyError, d.__delitem__, "one")

        # Test clear
        d.update(self._reference())
        d.clear()
        self.assertEqual(d, {})

        # Test copy()
        d.update(self._reference())
        da = d.copy()
        self.assertEqual(d, da)

        # Test keys, items, values
        r = self._reference()
        d.update(self._reference())
        for k in list(d.keys()):
            self.assertTrue(k in list(r.keys()))
        for i in list(d.items()):
            self.assertTrue(i in list(r.items()))
        for v in list(d.values()):
            self.assertTrue(v in list(r.values()))

        # Test has_key and "in".
        for i in list(r.keys()):
            self.assertTrue(i in d)
            self.assertTrue(i in d)

        # Test unhashability
        self.assertRaises(TypeError, hash, d)

    def test_stringmap_in_mapping(self):
        class A:
            def __init__(self):
                self.a = "a"
        self.assertEqual("a", "%(a)s" % A().__dict__)


def test_main():
    test_support.run_unittest(
        ClassDictTests,
        InstanceDictTests,
        PyStringMapTest
    )

if __name__ == "__main__":
    test_main()
