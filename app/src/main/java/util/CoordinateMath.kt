package util

import com.google.android.gms.maps.model.LatLng
import org.locationtech.jts.algorithm.MinimumBoundingCircle
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.abs

operator fun Coordinate.plus(other : Coordinate) = Coordinate(x + other.x, y + other.y)
operator fun Coordinate.minus(other : Coordinate) = Coordinate(x - other.x, y - other.y)
operator fun Coordinate.times(a : Double) = Coordinate(a*x, a*y)
operator fun Coordinate.div(a : Double): Coordinate = Coordinate(x/a, y/a)

operator fun Coordinate.unaryMinus() = Coordinate(-x, -y)
class CoordinateMath {
    companion object {
        /**
         * Converts a GPS location into Cartesian coordinates, given a reference point.
         *
         * @param location The GPS location to convert
         * @param reference The reference point (used as the origin)
         * @return The corresponding Cartesian coordinate in meters
         */
        public fun toCoordinate(location: LatLng, reference: LatLng): Coordinate {
            val earthRadius = 6371000.0

            val lat0 = Math.toRadians(reference.latitude)
            val lat = Math.toRadians(location.latitude)
            val lon = Math.toRadians(location.longitude)
            val lon0 = Math.toRadians(reference.longitude)

            val x = earthRadius * (lon - lon0) * cos(lat0)
            val y = earthRadius * (lat - lat0)

            return Coordinate(x, y)
        }

        /**
         * Calculates the radius of the smallest circle bounding all points.
         *
         * @param points The array of points to be bounded
         * @return The radius of the minimum bounding circle
         * */
        public fun minBoundingCircleRadius(points : Array<Coordinate>) : Double{
            return MinimumBoundingCircle(
                GeometryFactory().createMultiPointFromCoords(points)
            ).radius;
        }

        /**
         * Finds the shortest distance from origin to the given line segment.
         *
         * @param start The start of the line segment
         * @param end The end of the line segment
         * @return The distance to origin
         */
        public fun lineSegmentDistanceFromOrigin(start: Coordinate, end: Coordinate): Double {
            if (start.equals2D(end)) return Double.POSITIVE_INFINITY

            var segmentVector = end - start;
            val translatedOrigin = -start;

            val segmentLength = length(segmentVector)
            segmentVector /= segmentLength; //normalize

            val dot = dot(segmentVector, translatedOrigin)
            if (dot > segmentLength || dot < 0){ //out of bounds of line segment
                return minOf(length(start), length(end)) //use endpoints instead of perpendicular distance from the segment
            }

            return abs(dot(Coordinate(-segmentVector.y, segmentVector.x), translatedOrigin))
            // dot product with the vector perpendicular to `segmentVector` and the translated origin
        }

        /**
         * Calculates the dot product between two vectors (represented by `Coordinate`).
         *
         * @param v1 The first vector
         * @param v2 The second vector
         * @return Their dot product
         * */
        public fun dot(v1: Coordinate, v2: Coordinate): Double {
            return v1.x * v2.x + v1.y * v2.y;
        }

        /**
         * Calculates the length of a vector (represented by `Coordinate`).
         *
         * @param v The vector
         * @return The vector's length
         * */
        public fun length(v : Coordinate) : Double{
            return sqrt(v.x * v.x + v.y * v.y);
        }
    }
}