from PIL import Image, ImageDraw

def clean_advanced(image_path):
    img = Image.open(image_path).convert("RGBA")
    width, height = img.size
    pixels = img.load()
    
    # We'll use a flood fill from all 4 corners to find the background
    # Background colors in this specific image seem to be white/gray-ish
    # We'll also target any pixel that is very bright.
    
    mask = Image.new("L", (width, height), 0)
    for x in [0, width-1]:
        for y in [0, height-1]:
            ImageDraw.floodfill(img, (x, y), (0, 0, 0, 0), thresh=100)
    
    # After flood fill, some "islands" might remain.
    # We'll do a pass to remove anything that is roughly "checkered color"
    # or very bright (white/light gray)
    new_img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    new_pixels = new_img.load()
    
    for y in range(height):
        for x in range(width):
            r, g, b, a = img.getpixel((x, y))
            # If the pixel was zeroed by floodfill, or if it's very bright/neutral
            if a == 0:
                new_pixels[x, y] = (0, 0, 0, 0)
            elif r > 180 and g > 180 and b > 180 and abs(r-g) < 30 and abs(g-b) < 30:
                new_pixels[x, y] = (0, 0, 0, 0)
            else:
                new_pixels[x, y] = (r, g, b, a)
                
    # Crop to content
    bbox = new_img.getbbox()
    if bbox:
        new_img = new_img.crop(bbox)
        
    new_img.save(image_path, "PNG")

clean_advanced('/home/burak/Desktop/mobile-project/app/src/main/res/drawable/ic_paginex_icon.png')
print("Advanced cleaning complete")
