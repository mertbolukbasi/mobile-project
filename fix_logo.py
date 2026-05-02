from PIL import Image

def remove_background(image_path):
    img = Image.open(image_path).convert("RGBA")
    datas = img.getdata()

    newData = []
    for item in datas:
        r, g, b, a = item
        
        # Aggressive checkered background removal
        # Checkered patterns are usually pure white or pure light gray
        is_white = r > 240 and g > 240 and b > 240
        is_gray = (r > 190 and r < 210) and (g > 190 and g < 210) and (b > 190 and b < 210)
        
        # Also check for the "beige" background if it's there
        is_beige = (r > 240 and g > 230 and b > 200) and (abs(r-g) < 20)

        if is_white or is_gray or is_beige:
            newData.append((255, 255, 255, 0))
        else:
            newData.append(item)

    img.putdata(newData)
    img.save(image_path, "PNG")

remove_background('/home/burak/Desktop/mobile-project/app/src/main/res/drawable/ic_splash_logo.png')
print("Transparency applied")
