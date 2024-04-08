(function() {
    const en = [...document.querySelectorAll('span[lang=en]')]
    return en.map(span => span.innerText).find(text => text.trim())
})()