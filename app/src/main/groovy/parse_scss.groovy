import static groovy.io.FileType.FILES

def path = 'c:\\Users\\quewe\\Downloads\\'

new File(path).eachFileRecurse(FILES) {
    if (it.name.endsWith('.scss')) {
        String content = it.text
        String line = ''
        def sharpIndex = content.indexOf('#')
        while (sharpIndex >= 0) {
            def semicolonIndex = content.indexOf(';', sharpIndex)
            if (semicolonIndex == -1) {
                semicolonIndex = content.length() - 1
            }
            line += ',' + content.substring(sharpIndex, semicolonIndex).toUpperCase().replace('#', '0xFF')
            sharpIndex = content.indexOf('#', semicolonIndex)
        }
        println 'Arrays.asList(' + line.substring(1) + '),'
    }
}