package rsa.utils

class ArrayUtils {

    companion object{
        fun ByteArray.removeSubarrayFromArray(arrayB: ByteArray): ByteArray {
            val result = mutableListOf<Byte>()
            var i = 0
            while (i < this.size) {
                if (this[i] == arrayB[0]) {
                    var match = true
                    for (j in 1 until arrayB.size) {
                        if (i+j >= this.size || this[i+j] != arrayB[j]) {
                            match = false
                            break
                        }
                    }
                    if (match) {
                        i += arrayB.size
                        continue
                    }
                }
                result.add(this[i])
                i++
            }
            return result.toByteArray()
        }

        fun ByteArray.split(delimiter: ByteArray): List<ByteArray> {
            val result = mutableListOf<ByteArray>()
            var start = 0

            for (i in 0 .. this.size-delimiter.size) {
                if (this.copyOfRange(i, i + delimiter.size).contentEquals(delimiter)) {
                    result.add(this.copyOfRange(start, i))
                    start = i + delimiter.size
                }
            }

            if (start < this.size) {
                result.add(this.copyOfRange(start, this.size))
            }

            return result
        }
    }
}