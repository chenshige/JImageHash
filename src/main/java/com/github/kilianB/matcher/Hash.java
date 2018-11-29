package com.github.kilianB.matcher;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.math.BigInteger;

import com.github.kilianB.Require;
import com.github.kilianB.StringUtil;

/**
 * A wrapper class combining image hashes and their producing algorithm allowing
 * for meaningful analysis.
 * 
 * <p>
 * Hashes are bit encoded encoded values e.g. 0101011101
 * 
 * <p>
 * They are created from images down scaling information and enabling quick
 * comparison between instances produced by the same algorithm. Every bit in the
 * hash usually represents a section of the image containing certain information
 * (hue, brightness, color, frequencies or gradients)
 * 
 * @author Kilian
 * @since 1.0.0
 */
public class Hash {

	/**
	 * Unique identifier of the algorithm and settings used to create the hash
	 */
	private int algorithmId;

	/**
	 * Hash value representation
	 * 
	 * Hashes are constructed by left shifting BigIntegers with either Zero or One
	 * depending on the condition found in the image. Preceding 0's will be
	 * truncated therefore it is the algorithms responsibility to add a 1 padding
	 * bit at the beginning new BigInteger("011011) new BigInteger("000101) 1xxxxx
	 * 
	 */
	private BigInteger hashValue;
	// maybe move to bitsets//Mutable inetegers? not efficient for small keys?

	/** How many bits this hash has. 0 bits at the beginning are dropped */
	private int hashLength;

	/**
	 * Creates a Hash object with the specified hashValue and algorithmId. To allow
	 * save comparison of different hashes they have to be generated by the same
	 * algorithm.
	 * 
	 * @param hashValue   The hash value describing the image
	 * @param hashLength  the actual bit resolution of the hash. The bigInteger
	 *                    truncates leading zero bits resulting in a loss of length
	 *                    information.
	 * @param algorithmId Unique identifier of the algorithm used to create this
	 *                    hash
	 */
	public Hash(BigInteger hashValue, int hashLength, int algorithmId) {
		this.hashValue = hashValue;
		this.algorithmId = algorithmId;
		this.hashLength = hashLength;
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The hamming distance falls within [0-bitResolution]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * A longer hash (higher bitResolution) will increase the average hamming
	 * distance returned. While this method allows for the most accurate fine tuning
	 * of the distance {@link #normalizedHammingDistance(Hash)} is hash length
	 * independent.
	 * <p>
	 *
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will check if the hashes are compatible if no additional check is required
	 * see {@link #hammingDistanceFast(Hash)}
	 * 
	 * @param h The hash to calculate the distance to
	 * @return similarity value ranging between [0 - hash length]
	 */
	public int hammingDistance(Hash h) {
		if (this.algorithmId != h.algorithmId) {
			throw new IllegalArgumentException("Can't compare two hash values created by different algorithms");
		}
		return hammingDistanceFast(h);
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The hamming distance falls within [0-bitResolution]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * A longer hash (higher bitResolution) will increase the average hamming
	 * distance returned. While this method allows for the most accurate fine tuning
	 * of the distance {@link #normalizedHammingDistance(Hash)} is hash length
	 * independent.
	 * <p>
	 *
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will <b>NOT</b> check if the hashes are compatible.
	 * 
	 * @param h The hash to calculate the distance to
	 * @return similarity value ranging between [0 - hash length]
	 * @see #hammingDistance(Hash)
	 */
	public int hammingDistanceFast(Hash h) {
		return this.hashValue.xor(h.hashValue).bitCount();
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The hamming distance falls within [0-bitResolution]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * A longer hash (higher bitResolution) will increase the average hamming
	 * distance returned. While this method allows for the most accurate fine tuning
	 * of the distance {@link #normalizedHammingDistance(Hash)} is hash length
	 * independent.
	 * <p>
	 *
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will <b>NOT</b> check if the hashes are compatible.
	 * 
	 * @param bInt A big integer representing a hash
	 * @return similarity value ranging between [0 - hash length]
	 * @see #hammingDistance(Hash)
	 */
	public int hammingDistanceFast(BigInteger bInt) {
		return this.hashValue.xor(bInt).bitCount();
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The normalized hamming distance falls within [0-1]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * See {@link #hammingDistance(Hash)} for a non normalized version
	 * 
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will check if the hashes are compatible if no additional check is required
	 * see {@link #normalizedHammingDistanceFast(Hash)}
	 * 
	 * @param h The hash to calculate the distance to
	 * @return similarity value ranging between [0 - 1]
	 */
	public double normalizedHammingDistance(Hash h) {
		if (this.algorithmId != h.algorithmId) {
			throw new IllegalArgumentException("Can't compare two hash values created by different algorithms");
		}
		// We expect both integers to contain the same bit key lengths!
		// -1 due to the preceding padding bit
		return normalizedHammingDistanceFast(h);
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The normalized hamming distance falls within [0-1]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * See {@link #hammingDistance(Hash)} for a non normalized version
	 *
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will <b>NOT</b> check if the hashes are compatible.
	 * 
	 * @param h The hash to calculate the distance to
	 * @return similarity value ranging between [0 - 1]
	 * @see #hammingDistance(Hash)
	 */
	public double normalizedHammingDistanceFast(Hash h) {
		// We expect both integers to contain the same bit key lengths!
		return hammingDistanceFast(h) / (double) hashLength;
	}

	/**
	 * Check if the bit at the given position is set
	 * 
	 * @param position of the bit. An index of 0 points to the lowest (rightmost
	 *                 bit)
	 * @return true if the bit is set (1) or false if it's not set (0)
	 * @throws IllegalArgumentException if the supplied index is outside the hash
	 *                                  bound
	 * @since 2.0.0
	 */
	public boolean getBit(int position) {
		Require.inRange(position, 0, this.getBitResolution() - 1, "Bit out of bounds");
		return getBitUnsafe(position);
	}

	/**
	 * Check if the bit at the given position of the hash is set. This method does
	 * not check the bounds of the supplied argument.
	 * 
	 * @param position of the bit. An index of 0 points to the lowest (rightmost
	 *                 bit)
	 * @return true if the bit is set (1). False if it's not set (0) ot the index is
	 *         bigger than the hash length.
	 * @throws ArithmeticException if position is negative
	 * @since 2.0.0
	 */
	public boolean getBitUnsafe(int position) {
		return hashValue.testBit(position);
	}

	/**
	 * Return the algorithm identifier specifying by which algorithm and setting
	 * this hash was created. The id shall remain constant.
	 * 
	 * @return The algorithm id
	 */
	public int getAlgorithmId() {
		return algorithmId;
	}

	/**
	 * @return the base BigInteger holding the hash value
	 */
	public BigInteger getHashValue() {
		return hashValue;
	}

	/**
	 * Creates a visual representation of the hash mapping the hash values to the
	 * section of the rescaled image used to generate the hash.
	 * 
	 * starting with version 2.0.0 this method returns a rotated and mirrored.
	 * image. Could be added as a fix but it's not a high priority right now.
	 * 
	 * @param blockSize Stretch factor.Due to rescaling the image was shrunk down
	 *                  during hash creation.
	 * @return A black and white image representing the individual bits of the hash
	 */
	public BufferedImage toImage(int blockSize) {
		int width = (int) Math.sqrt((hashValue.bitLength() - 1));
		int height = width;

		int white = Color.WHITE.getRGB();
		int black = Color.BLACK.getRGB();

		BufferedImage bi = new BufferedImage(blockSize * width, blockSize * height, BufferedImage.TYPE_BYTE_GRAY);

		int i = hashValue.bitLength() - 1;
		for (int w = 0; w < width * blockSize; w = w + blockSize) {
			for (int h = 0; h < height * blockSize; h = h + blockSize) {
				boolean bit = hashValue.testBit(i);
				for (int m = 0; m < blockSize; m++) {
					for (int n = 0; n < blockSize; n++) {
						int x = w + m;
						int y = h + n;
						bi.setRGB(y, x, bit ? black : white);
					}
				}
				i--;
			}
		}
		return bi;
	}

	/**
	 * @return the hash resolution in bits
	 */
	public int getBitResolution() {
		return hashLength;
	}

	/**
	 * Return the byte representation of the big integer with the leading zero byte
	 * stripped if present. The BigInteger class prepends a sign byte if necessary
	 * to indicate the signum of the number. Since our hashes are always positive we
	 * can get rid of it and reduce the space requirement in our db by 1 byte.
	 * 
	 * <p>
	 * To reconstruct the big integer value we can simply prepend a [0x00] byte even
	 * if it wasn't present in the first place. The constructor
	 * {@link java.math.BigInteger#BigInteger(byte[])} will take care of it.
	 * 
	 * @return the byte representation of the big integer without an artificial sign
	 *         byte.
	 */
	public byte[] toByteArray() {
		byte[] bArray = hashValue.toByteArray();

		if (bArray[0] != 0) {
			return bArray;
		} else {
			byte[] bArrayWithoutSign = new byte[bArray.length - 1];
			System.arraycopy(bArray, 1, bArrayWithoutSign, 0, bArray.length - 1);
			return bArrayWithoutSign;
		}

	}

	public String toString() {
		return "Hash: " + StringUtil.fillString("0", hashLength, hashValue.toString(2)) + " [algoId: " + algorithmId
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + algorithmId;
		result = prime * result + ((hashValue == null) ? 0 : hashValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hash other = (Hash) obj;
		if (algorithmId != other.algorithmId)
			return false;
		if (hashValue == null) {
			if (other.hashValue != null)
				return false;
		} else if (!hashValue.equals(other.hashValue))
			return false;
		return true;
	}

}
