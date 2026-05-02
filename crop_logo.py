from PIL import Image

def crop_for_topbar(image_path, output_path):
    img = Image.open(image_path).convert("RGBA")
    width, height = img.size
    
    # The text "Paginex" is at the bottom. 
    # Usually it takes up about 30% of the height.
    # We'll crop the top 70% to get the icon.
    icon_part = img.crop((0, 0, width, int(height * 0.7)))
    
    # Trim transparency
    bbox = icon_part.getbbox()
    if bbox:
        icon_part = icon_part.crop(bbox)
        
    icon_part.save(output_path, "PNG")
    print(f"Icon saved to {output_path}")

crop_for_topbar('/home/burak/Desktop/mobile-project/app/src/main/res/drawable/ic_splash_logo.png', 
                '/home/burak/Desktop/mobile-project/app/src/main/res/drawable/ic_paginex_icon.png')
