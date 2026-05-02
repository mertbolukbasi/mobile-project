from PIL import Image

try:
    img = Image.open('/home/burak/Desktop/mobile-project/app/src/main/res/drawable/ic_paginex_icon.png')
    img = img.convert("RGBA")
    datas = img.getdata()

    newData = []
    # AI generated images have slightly off-white backgrounds (e.g., beige-ish).
    # We will remove pixels that are very bright (R,G,B > 230)
    for item in datas:
        # Check if the pixel is bright enough to be background
        if item[0] > 220 and item[1] > 220 and item[2] > 200: 
            # Check if it's relatively neutral/beige
            if abs(item[0] - item[1]) < 25 and item[0] > item[2]:
                newData.append((255, 255, 255, 0))
            elif item[0] > 240 and item[1] > 240 and item[2] > 240:
                newData.append((255, 255, 255, 0))
            else:
                newData.append(item)
        else:
            newData.append(item)

    img.putdata(newData)
    img.save('/home/burak/Desktop/mobile-project/app/src/main/res/drawable/ic_paginex_icon.png', "PNG")
    print("Success")
except Exception as e:
    print(e)
