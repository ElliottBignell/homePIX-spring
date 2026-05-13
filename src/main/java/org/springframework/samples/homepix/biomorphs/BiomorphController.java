package org.springframework.samples.homepix.biomorphs;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/svg")
public class BiomorphController {

    @GetMapping(value = "/wing", produces = "image/svg+xml")
    public String generateWing(
            @RequestParam(defaultValue = "4") int initialVeins,
            @RequestParam(defaultValue = "4") int iterations,
            @RequestParam(defaultValue = "40") int branchDistance,
            @RequestParam(defaultValue = "600") int width,
            @RequestParam(defaultValue = "400") int height) {

        WingGenerator generator = new WingGenerator(initialVeins, iterations, branchDistance, width, height);
        return generator.generateSVG();
    }

    private static class WingGenerator {
        private final int initialVeins;
        private final int iterations;
        private final int branchDistance;
        private final int width;
        private final int height;
        private final List<Vein> veins = new ArrayList<>();
        private List<Point> currentVeinEnds = new ArrayList<>();

        // private final String[] colors = {"#000000", "#FF0000", "#0000FF", "#00AA00", "#FFA500"};

        public WingGenerator(int initialVeins, int iterations, int branchDistance, int width, int height) {
            this.initialVeins = initialVeins;
            this.iterations = iterations;
            this.branchDistance = branchDistance;
            this.width = width;
            this.height = height;
        }

        public String generateSVG() {
            Point root = new Point(50, height / 2);

            // Create initial veins (iteration 0)
            createInitialVeins(root);

            // Perform iterations of branching
            for (int i = 0; i < iterations; i++) {
                branchVeins(i + 1);
            }

            return buildSVG();
        }

        private void createInitialVeins(Point root) {
            int startY = height / 3;
            int endY = 2 * height / 3;
            int yStep = (endY - startY) / (initialVeins - 1);

            for (int i = 0; i < initialVeins; i++) {
                int y = startY + i * yStep;
                Point end = new Point(150 + i * 20, y);

                veins.add(new Vein(root, end, 0));
                currentVeinEnds.add(end);
            }
        }

        private void branchVeins(int iteration) {
            List<Point> newVeinEnds = new ArrayList<>();

            // Each vein end branches into two
            for (int i = 0; i < currentVeinEnds.size(); i++) {
                Point veinEnd = currentVeinEnds.get(i);

                // Create two branch points
                Point branch1 = new Point(
                    veinEnd.x + branchDistance,
                    veinEnd.y - branchDistance/3 + randomOffset()
                );

                Point branch2 = new Point(
                    veinEnd.x + branchDistance,
                    veinEnd.y + branchDistance/3 + randomOffset()
                );

                // Add branching veins
                veins.add(new Vein(veinEnd, branch1, iteration));
                veins.add(new Vein(veinEnd, branch2, iteration));

                newVeinEnds.add(branch1);
                newVeinEnds.add(branch2);
            }

            currentVeinEnds = newVeinEnds;
        }

        private int randomOffset() {
            return (int)(Math.random() * 8 - 4);
        }

        private String buildSVG() {
            StringBuilder svg = new StringBuilder();
            svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
               .append("width=\"").append(width).append("\" ")
               .append("height=\"").append(height).append("\" ")
               .append("viewBox=\"0 0 ").append(width).append(" ").append(height).append("\">\n");

            // Background
            svg.append("  <rect width=\"").append(width).append("\" height=\"").append(height)
               .append("\" fill=\"#f8f8f8\" />\n");

            // Legend
            svg.append("  <g transform=\"translate(20, 30)\" font-family=\"Arial\" font-size=\"12\">\n");
            svg.append("    <text x=\"0\" y=\"0\" font-weight=\"bold\">Iteration colors:</text>\n");
            for (int i = 0; i <= iterations; i++) {
                svg.append("    <line x1=\"0\" y1=\"").append(15 + i * 20)
                   .append("\" x2=\"20\" y2=\"").append(15 + i * 20)
                   .append("\" stroke=\"").append("#000000")
					//.append("\" stroke=\"").append(colors[i % colors.length])
                   .append("\" stroke-width=\"2\" />\n");
                svg.append("    <text x=\"25\" y=\"").append(19 + i * 20)
                   .append("\">Iteration ").append(i).append("</text>\n");
            }
            svg.append("  </g>\n");

            // Draw quadrilaterals as semi-transparent fills first
            svg.append("  <!-- Quadrilateral fills -->\n");
            for (int i = 0; i < iterations; i++) {
                svg.append("  <g fill=\"rgba(200, 220, 240, 0.1)\" stroke=\"none\">\n");
                // Find and draw quadrilaterals from iteration i
                drawQuadrilaterals(svg, i);
                svg.append("  </g>\n");
            }

            // Draw veins by iteration
            Map<Integer, List<Vein>> veinsByIteration = veins.stream()
                .collect(Collectors.groupingBy(v -> v.iteration));

			List<Vein> endVeins = new ArrayList<>();

            for (int i = 0; i <= iterations; i++) {

				List<Vein> iterationVeins = veinsByIteration.getOrDefault(i, new ArrayList<>());

				if (!iterationVeins.isEmpty()) {

					if (i == iterations) {
						for (int vein = 1; vein < iterationVeins.size() - 1; vein += 2) {
							iterationVeins.get(vein + 1).end = iterationVeins.get(vein).end;
						}
					}

					svg.append("  <g stroke=\"").append("#000000")
                       .append("\" stroke-width=\"").append(i == 0 ? "2.5" : "1.8")
                       .append("\" fill=\"none\">\n");

                    for (Vein vein : iterationVeins) {
                        svg.append("    <line x1=\"").append(vein.start.x)
                           .append("\" y1=\"").append(vein.start.y)
                           .append("\" x2=\"").append(vein.end.x)
                           .append("\" y2=\"").append(vein.end.y)
                           .append("\" />\n");
                    }
                    svg.append("  </g>\n");

					endVeins = iterationVeins;
                }
            }

			svg.append("  <g stroke=\"").append("#000000")
				.append("\" stroke-width=\"").append("1.8")
				.append("\" fill=\"none\">\n");

			for (int i = 0; i < endVeins.size() - 1; i += 2) {

				Point first = endVeins.get(i).end;
				Point second = endVeins.get(i + 1).end;

				svg.append("    <line x1=\"").append(first.x)
					.append("\" y1=\"").append(first.y)
					.append("\" x2=\"").append(first.x + 10)
					.append("\" y2=\"").append(first.y + 10)
					.append("\" />\n");

				svg.append("    <line x1=\"").append(second.x)
					.append("\" y1=\"").append(second.y)
					.append("\" x2=\"").append(first.x + 10)
					.append("\" y2=\"").append(first.y + 10)
					.append("\" />\n");
			}
			svg.append("  </g>\n");

            svg.append("</svg>");
            return svg.toString();
        }

        private void drawQuadrilaterals(StringBuilder svg, int iteration) {
            // This is a simplified version - in a real implementation you'd
            // need to reconstruct the quadrilaterals from the vein structure
            // For now, we'll just draw some sample quadrilaterals
            if (currentVeinEnds.size() >= 4) {
                for (int i = 0; i < currentVeinEnds.size() - 3; i += 2) {
                    if (i + 3 < currentVeinEnds.size()) {
                        Point p1 = currentVeinEnds.get(i);
                        Point p2 = currentVeinEnds.get(i + 1);
                        Point p3 = currentVeinEnds.get(i + 3);
                        Point p4 = currentVeinEnds.get(i + 2);

                        svg.append("    <polygon points=\"")
                           .append(p1.x).append(",").append(p1.y).append(" ")
                           .append(p2.x).append(",").append(p2.y).append(" ")
                           .append(p3.x).append(",").append(p3.y).append(" ")
                           .append(p4.x).append(",").append(p4.y).append(" ")
                           .append("\" />\n");
                    }
                }
            }
        }

        private static class Point {
            int x, y;
            Point(int x, int y) { this.x = x; this.y = y; }
        }

        private static class Vein {
            Point start, end;
            int iteration;
            Vein(Point start, Point end, int iteration) {
                this.start = start;
                this.end = end;
                this.iteration = iteration;
            }
        }
    }
}

