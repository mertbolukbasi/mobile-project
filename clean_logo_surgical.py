from PIL import Image, ImageOps

def clean(image_path):
    img = Image.open(image_path).convert("RGBA")
    # Resize slightly to anti-alias if needed, but let's stay native first
    pixels = img.getdata()

    new_pixels = []
    
    # Logo colors (approximate ranges)
    # Blue: low R, mid G, mid B
    # Orange/Red: high R, mid G, low B
    # Gold: high R, high G, low B
    # Dark Navy: very low everything
    
    for r, g, b, a in pixels:
        # Distance to pure white
        dist_white = ((r-255)**2 + (g-255)**2 + (b-255)**2)**0.5
        # Checkered gray is often around (200, 200, 200) or (240, 240, 240)
        dist_gray1 = ((r-204)**2 + (g-204)**2 + (b-204)**2)**0.5
        dist_gray2 = ((r-238)**2 + (g-238)**2 + (b-238)**2)**0.5
        
        # If it's close to any of these background colors, make it transparent
        if dist_white < 60 or dist_gray1 < 40 or dist_gray2 < 30:
            new_pixels.append((0, 0, 0, 0))
        elif r > 200 and g > 200 and b > 200: # General bright stuff
            new_pixels.append((0, 0, 0, 0))
        else:
            # Keep the pixel
            new_pixels.append((r, g, b, a))

    img.putdata(new_pixels)
    
    # Optional: Crop the logo to its content to avoid wasted space
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
        
    img.save(image_path, "PNG")

clean('/Users/ramazanbirkan/Desktop/Mobile_Application/app/src/main/res/drawable/ic_splash_logo.png')
print("Surgical cleaning done")
